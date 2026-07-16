process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

const express = require('express');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());

// 14 間運動中心清單（新增 area 屬性，劃分台北市與新北市）[cite: 1]
const SPORTS_CENTERS = [
    { name: '內湖', type: 'CYC', url: 'https://nhsc.cyc.org.tw/api', area: '台北市' },
    { name: '南港', type: 'CYC', url: 'https://ngsc.cyc.org.tw/api', area: '台北市' },
    { name: '松山', type: 'SONGSHAN', url: 'https://sssc.com.tw/', area: '台北市' },
    { name: '文山', type: 'CYC', url: 'https://wssc.cyc.org.tw/api', area: '台北市' },
    { name: '中山', type: 'CYC', url: 'https://cssc.cyc.org.tw/api', area: '台北市' },
    { name: '汐止', type: 'CYC', url: 'https://xzcsc.cyc.org.tw/api', area: '新北市' }, // 👈 新北市[cite: 1]
    { name: '永和', type: 'CYC', url: 'https://yhcsc.cyc.org.tw/api', area: '新北市' }, // 👈 新北市[cite: 1]
    { name: '士林', type: 'SHILIN', url: 'https://www.slsc-taipei.org/', area: '台北市' },
    { name: '中正', type: 'SHILIN', url: 'https://wsjjsc.com.tw/', area: '台北市' },
    { name: '大安', type: 'SHILIN', url: 'https://www.daansports.com.tw/zh_TW/onsitenum', area: '台北市' },
    { name: '北投', type: 'SHILIN', url: 'https://www.btsport.org.tw/zh-TW/onsitenum', area: '台北市' },
    { name: '信義', type: 'XINYI', url: 'https://webapi.teamxports.com/api/web/carosel/get-court-cat-people-flow?siteId=4', area: '台北市' },
    { name: '萬華', type: 'SONGSHAN', url: 'https://whsc.com.tw/', area: '台北市' },
    { name: '大同', type: 'SHILIN', url: 'https://www.dtsc-wdyg.com.tw/', area: '台北市' },
    { name: '板橋', type: 'SHILIN', url: 'https://www.bqsports.com.tw/zh_TW/onsitenum?t=202510271600', area: '新北市' },
    { name: '中和', type: 'MRAYTEC', url: 'https://zhs.mraytec.com/', area: '新北市' },
    { name: '新店', type: 'SHILIN', url: 'https://www.xdsports.com.tw/zh_TW/onsitenum', area: '新北市' },
    { name: '淡水', type: 'SHILIN', url: 'https://wstssc.com.tw/', area: '新北市' },
    { name: '蘆洲？', type: 'SHILIN', url: 'https://lzsc.chanchao.com.tw/', area: '新北市' },
    { name: '林口', type: 'CYC', url: 'https://lkcsc.cyc.org.tw/api', area: '新北市' },
    { name: '新莊？', type: 'SHILIN', url: 'https://www.xzsports.com.tw/', area: '新北市' },
    { name: '三重', type: 'CYC', url: 'http://www.scsports.com.tw/proxy1.php', area: '新北市' },
    { name: '鶯歌', type: 'XINYI', url: 'https://webapi.teamxports.com/api/web/carosel/get-court-cat-people-flow?siteId=6', area: '新北市' },
    { name: '泰山', type: 'SHILIN', url: 'https://wstscsc.com.tw/', area: '新北市' },
    { name: '三峽', type: 'XINYI', url: 'https://webapi.teamxports.com/api/web/carosel/get-court-cat-people-flow?siteId=7', area: '新北市' },
    { name: '土城', type: 'CYC', url: 'https://www.tcsports.com.tw/proxy1.php', area: '新北市' },
    { name: '苓雅', type: 'SHILIN', url: 'https://klyscwdyg.com.tw/', area: '高雄市' },
    { name: '鳳山', type: 'SHILIN', url: 'https://kfspwdyg.com.tw/', area: '高雄市' },
    { name: '岡山', type: 'SHILIN', url: 'https://wskgsc.com.tw/', area: '高雄市' },
    { name: '朝馬', type: 'CYC', url: 'https://cmcsc.cyc.org.tw/api/', area: '台中市' },
    { name: '中壢', type: 'CYC', url: 'https://zlcsc.cyc.org.tw/api/', area: '桃園市' },
    { name: '桃園', type: 'CYC', url: 'https://tycsc.cyc.org.tw/api/', area: '桃園市' },
    { name: '八德', type: 'CYC', url: 'https://bdcsc.cyc.org.tw/api/', area: '桃園市' },
    { name: '北屯', type: 'CYC', url: 'https://btcsc.cyc.org.tw/api/', area: '台中市' },
    { name: '竹光', type: 'CYC', url: 'https://zgcsc.cyc.org.tw/api', area: '新竹市' },
    { name: '彰南', type: 'SHILIN', url: 'https://changnan.jcswim.com.tw/index.html', area: '彰化縣' },
];

async function fetchAndParseMraytec(center, signal) {
  const [gymRes, swimRes] = await Promise.all([
    fetch(`${center.url}/state/fitness`, { signal }),
    fetch(`${center.url}/state/pool`, { signal })
  ]);

  if (!gymRes.ok || !swimRes.ok) throw new Error('Mraytec API 回應失敗');

  const gymData = await gymRes.json();   // 預期格式: { value: X }
  console.log(`[解析日誌] ${center.name} 健身房原始 JSON 資料:`, gymData);
  const swimData = await swimRes.json(); // 預期格式: { value: Y }
  console.log(`[解析日誌] ${center.name} 游泳池原始 JSON 資料:`, swimData);

  return {
    name: center.name,
    gym: { 
      current: parseInt(gymData.value, 10), 
      max: 120 // 靜態容留人數[cite: 1]
    },
    swim: { 
      current: parseInt(swimData.value, 10), 
      max: 300 // 靜態容留人數[cite: 1]
    },
    status: 'online'
  };
}

// 【解析器 1】救國團 API 處理[cite: 1]
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
  if(!data.data || data.data.length < 2) {
    return {
      name,
      gym: { current: parseInt(data.data[0].currCapacity, 10), max: parseInt(data.data[0].upperBound, 10) },
      swim: { current: 0, max: 0 },
      status: 'online'
    };
  }
  return {
    name,
    gym: { current: parseInt(data.data[1].currCapacity, 10), max: parseInt(data.data[1].upperBound, 10) },
    swim: { current: parseInt(data.data[0].currCapacity, 10) ?? 0, max: parseInt(data.data[0].upperBound, 10)?? 0 },
    status: 'online'
  };
}

// 【解析器 2】士林網頁解析（純文字特徵型）[cite: 1]
function parseShilin(cleanHtml, name) {
  //console.log(`[解析日誌] 士林中心原始 HTML 資料:`, cleanHtml);
  const gymMatch = cleanHtml.match(/健身房[^\d]*(\d+)[^\d]*人[^\d]*容留[^\d]*(\d+)/) || cleanHtml.match(/健身房.*?(\d+).*?(\d+)/) || cleanHtml.match(/體適能中心.*?(\d+).*?(\d+)/);
  const swimMatch = cleanHtml.match(/游泳池[^\d]*(\d+)[^\d]*人[^\d]*容留[^\d]*(\d+)/) || cleanHtml.match(/游泳池.*?(\d+).*?(\d+)/);

  return {
    name,
    gym: { current: gymMatch ? parseInt(gymMatch[1], 10) : 0, max: gymMatch ? parseInt(gymMatch[2], 10) : 100 },
    swim: { current: swimMatch ? parseInt(swimMatch[1], 10) : 0, max: swimMatch ? parseInt(swimMatch[2], 10) : 200 },
    status: (gymMatch || swimMatch) ? 'online' : 'offline',
    message: (gymMatch || swimMatch) ? null : '士林解析失敗'
  };
}

// 【解析器 3】松山網頁解析[cite: 1]
function parseSongshan(cleanHtml, name) {
  const gymMatch = cleanHtml.match(/健身房.*?MaxQty[^\d]*(\d+)[^\d]*UseQty[^\d]*(\d+)/);
  const swimMatch = cleanHtml.match(/游泳池.*?MaxQty[^\d]*(\d+)[^\d]*UseQty[^\d]*(\d+)/);

  const isSuccess = gymMatch || swimMatch;

  return {
    name,
    gym: { 
      current: gymMatch ? parseInt(gymMatch[2], 10) : 0, 
      max: gymMatch ? parseInt(gymMatch[1], 10) : 0    
    },
    swim: { 
      current: swimMatch ? parseInt(swimMatch[2], 10) : 0, 
      max: swimMatch ? parseInt(swimMatch[1], 10) : 0    
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
      const timeoutId = setTimeout(() => controller.abort(), 10000);

      const response = await fetch(center.url, { 
        signal: controller.signal,
        headers: { 
          'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36',
          'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7'
        } 
      });
      clearTimeout(timeoutId);

      if (!response.ok && center.type !== 'MRAYTEC') throw new Error(`HTTP ${response.status}`);

      let parsedResult;

      // 依據中心類型分流解析[cite: 1]
      if (center.type === 'CYC') {
        const data = await response.json();
        parsedResult = parseCYC(data, center.name);
      } else if (center.type === 'XINYI') {
        const data = await response.json();
        parsedResult = parseXINYI(data, center.name);
      } else if (center.type === 'MRAYTEC') {
        // 呼叫封裝後的非同步函式[cite: 1]
        parsedResult = await fetchAndParseMraytec(center, controller.signal);
        clearTimeout(timeoutId);
      } else {
        const htmlText = await response.text();
        console.log(`[連線日誌] ${center.name} `);
        const cleanHtml = htmlText.replace(/\s+/g, ' '); 

        if (center.type === 'SHILIN') {
          parsedResult = parseShilin(cleanHtml, center.name);
        } else if (center.type === 'SONGSHAN') {
          parsedResult = parseSongshan(cleanHtml, center.name);
        } else {
          parsedResult = {
            name: center.name,
            gym: { current: 0, max: 0 },
            swim: { current: 0, max: 0 },
            status: 'online',
            message: '待補齊專屬 Regex 規則'
          };
        }
      }

      // 將該運動中心的 area 屬性注入回傳結果[cite: 1]
      return { ...parsedResult, area: center.area };

    } catch (error) {
      console.error(`[錯誤日誌] ${center.name} 連線或解析失敗:`, error.message);
      return {
        name: center.name,
        area: center.area, // 斷線時也回傳地區，確保前端仍能正確篩選卡片[cite: 1]
        status: 'offline',
        message: '連線失敗'
      };
    }
  });

  const results = await Promise.all(fetchPromises);
  res.json(results);
});

app.listen(PORT, () => {
  console.log(`[Proxy Server] 已啟動：http://localhost:${PORT}`);
});