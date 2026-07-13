process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());

// 14 間運動中心清單（完全對齊你提供的正確網址）
const SPORTS_CENTERS = [
  { name: '內湖', type: 'CYC', url: 'https://nhsc.cyc.org.tw/api' },
  { name: '南港', type: 'CYC', url: 'https://ngsc.cyc.org.tw/api' },
  { name: '松山', type: 'SONGSHAN', url: 'https://sssc.com.tw/' }, // 👈 走專屬松山解析
  { name: '文山', type: 'CYC', url: 'https://wssc.cyc.org.tw/api' },
  { name: '中山', type: 'CYC', url: 'https://cssc.cyc.org.tw/api' },
  { name: '汐止', type: 'CYC', url: 'https://xzcsc.cyc.org.tw/api' },
  { name: '永和', type: 'CYC', url: 'https://yhcsc.cyc.org.tw/api' },
  { name: '士林', type: 'SHILIN', url: 'https://www.slsc-taipei.org/' }, // 👈 走專屬士林解析
  { name: '中正', type: 'SHILIN', url: 'https://wsjjsc.com.tw/' },
  { name: '大安', type: 'SHILIN', url: 'https://www.daansports.com.tw/zh_TW/onsitenum' },
  { name: '北投', type: 'SHILIN', url: 'https://www.btsport.org.tw/zh-TW/onsitenum' },
  { name: '信義', type: 'XINYI', url: 'https://webapi.teamxports.com/api/web/carosel/get-court-cat-people-flow?siteId=4' },
  { name: '萬華', type: 'SONGSHAN', url: 'https://whsc.com.tw/' },
  { name: '大同', type: 'SHILIN', url: 'https://www.dtsc-wdyg.com.tw/' }
];

// 【解析器 1】救國團 API 處理
function parseCYC(data, name) {
  return {
    name,
    gym: { current: parseInt(data.gym[0], 10), max: parseInt(data.gym[1], 10) },
    swim: { current: parseInt(data.swim[0], 10), max: parseInt(data.swim[1], 10) },
    status: 'online'
  };
}

function parseXINYI(data, name) {
  console.log(`[解析日誌] 信義中心原始 JSON 資料:`, data);
  return {
    name,
    gym: { current: parseInt(data.data[1].currCapacity, 10), max: parseInt(data.data[1].upperBound, 10) },
    swim: { current: parseInt(data.data[0].currCapacity, 10), max: parseInt(data.data[0].upperBound, 10) },
    status: 'online'
  };
}

// 【解析器 2】士林網頁解析（純文字特徵型）
function parseShilin(cleanHtml, name) {
  const gymMatch = cleanHtml.match(/健身房[^\d]*(\d+)[^\d]*人[^\d]*容留[^\d]*(\d+)/) || cleanHtml.match(/健身房.*?(\d+).*?(\d+)/);
  const swimMatch = cleanHtml.match(/游泳池[^\d]*(\d+)[^\d]*人[^\d]*容留[^\d]*(\d+)/) || cleanHtml.match(/游泳池.*?(\d+).*?(\d+)/);

  return {
    name,
    gym: { current: gymMatch ? parseInt(gymMatch[1], 10) : 0, max: gymMatch ? parseInt(gymMatch[2], 10) : 100 },
    swim: { current: swimMatch ? parseInt(swimMatch[1], 10) : 0, max: swimMatch ? parseInt(swimMatch[2], 10) : 200 },
    status: (gymMatch || swimMatch) ? 'online' : 'offline',
    message: (gymMatch || swimMatch) ? null : '士林解析失敗'
  };
}

// 【解析器 3】松山網頁解析（🎯 研判指令碼 JSON 字串型）
function parseSongshan(cleanHtml, name) {
  // 核心邏輯：無視換行與斜線，直接撈出 MachineName 後面的 MaxQty 與 UseQty
  // 捕獲組 [1] 是 MaxQty (上限)，捕獲組 [2] 是 UseQty (目前人數)
  const gymMatch = cleanHtml.match(/健身房.*?MaxQty[^\d]*(\d+)[^\d]*UseQty[^\d]*(\d+)/);
  const swimMatch = cleanHtml.match(/游泳池.*?MaxQty[^\d]*(\d+)[^\d]*UseQty[^\d]*(\d+)/);

  const isSuccess = gymMatch || swimMatch;

  return {
    name,
    gym: { 
      current: gymMatch ? parseInt(gymMatch[2], 10) : 0, // UseQty
      max: gymMatch ? parseInt(gymMatch[1], 10) : 0    // MaxQty
    },
    swim: { 
      current: swimMatch ? parseInt(swimMatch[2], 10) : 0, // UseQty
      max: swimMatch ? parseInt(swimMatch[1], 10) : 0    // MaxQty
    },
    status: isSuccess ? 'online' : 'offline',
    message: isSuccess ? null : '松山內部資料解析失敗'
  };
}

// API 主路由
app.get('/sports-centers', async (req, res) => {
  const fetchPromises = SPORTS_CENTERS.map(async (center) => {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 6000);

      const response = await fetch(center.url, { 
        signal: controller.signal,
        headers: { 
          'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
          'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8'
        } 
      });
      clearTimeout(timeoutId);

      if (!response.ok) throw new Error(`HTTP ${response.status}`);

      // 救國團走 JSON，其他一律先拿 HTML 文字
      if (center.type === 'CYC') {
        const data = await response.json();
        return parseCYC(data, center.name);
      } else if (center.type === 'XINYI') {
        const data = await response.json();
        return parseXINYI(data, center.name);
      }

      const htmlText = await response.text();
      //console.log(`[連線日誌] ${center.name} 成功取得 HTML，長度: ${htmlText.length} 字元`);
      //console.log(`[連線日誌] ${center.name} HTML :`, htmlText);
      const cleanHtml = htmlText.replace(/\s+/g, ' '); // 統一壓扁成單行文字

      // 依據中心類型精準分流
      if (center.type === 'SHILIN') {
        return parseShilin(cleanHtml, center.name);
      } else if (center.type === 'SONGSHAN') {
        return parseSongshan(cleanHtml, center.name);
      } else {
        // 其他 HTML 類型站點先給予預設值防呆
        return {
          name: center.name,
          gym: { current: 0, max: 0 },
          swim: { current: 0, max: 0 },
          status: 'online',
          message: '待補齊專屬 Regex 規則'
        };
      }
    } catch (error) {
      console.error(`[連線日誌] ${center.name} 失敗:`, error.message);
      return {
        name: center.name,
        status: 'offline',
        message: '連線失敗'
      };
    }
  });

  const results = await Promise.all(fetchPromises);
  res.json(results);
});

app.listen(PORT, () => {
  console.log(`[Proxy Server] 混合數據源完全體已啟動：http://localhost:${PORT}`);
});