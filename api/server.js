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
    { name: '中正', type: 'TPSC_POST', url: 'https://booking-tpsc.sporetrofit.com/Home/loadLocationPeopleNum', area: '台北市', lid: 'JJSC' },
    { name: '大安', type: 'SHILIN', url: 'https://www.daansports.com.tw/zh_TW/onsitenum', area: '台北市' },
    { name: '北投', type: 'SHILIN', url: 'https://www.btsport.org.tw/zh-TW/onsitenum', area: '台北市' },
    { name: '信義', type: 'XINYI', url: 'https://webapi.teamxports.com/api/web/carosel/get-court-cat-people-flow?siteId=4', area: '台北市' },
    { name: '萬華', type: 'SONGSHAN', url: 'https://whsc.com.tw/', area: '台北市' },
    { name: '大同', type: 'SHILIN', url: 'https://www.dtsc-wdyg.com.tw/', area: '台北市' },
    { name: '板橋', type: 'SHILIN', url: 'https://www.bqsports.com.tw/zh_TW/onsitenum?t=202510271600', area: '新北市' },
    { name: '中和', type: 'MRAYTEC', url: 'https://zhs.mraytec.com/', area: '新北市' },
    { name: '新店', type: 'SHILIN', url: 'https://www.xdsports.com.tw/zh_TW/onsitenum', area: '新北市' },
    { name: '淡水', type: 'SHILIN', url: 'https://wstssc.com.tw/', area: '新北市' },
    { name: '林口', type: 'CYC', url: 'https://lkcsc.cyc.org.tw/api', area: '新北市' },
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
    { name: '永華', type: 'TEAM_MOVEMENT', url: 'https://teamweb.sporetrofit.com/Location/', area: '台南市', postData: 'LID=YHSC&LIDName=%E6%B0%B8%E8%8F%AF%E5%9C%8B%E6%B0%91%E9%81%8B%E5%8B%95%E4%B8%AD%E5%BF%83&CategoryImgURL=https%3A%2F%2Fbooking-tpsc.sporetrofit.com%3A8081%2FLOHASIcon%2FMenuItems%2FCS20240524%2F&address=700%E5%8F%B0%E5%8D%97%E5%B8%82%E4%B8%AD%E8%A5%BF%E5%8D%80%E4%B8%AD%E8%8F%AF%E8%A5%BF%E8%B7%AF%E4%BA%8C%E6%AE%B530%E8%99%9F&phone=%2806%29298-6111&businessHours=06%3A00%7E22%3A00&imageUrl=https%3A%2F%2Fresource.sporetrofit.com%2FWebAdmin%2Fimage%2FCS%2FLocation%2FYHSC%2FLocationPic%2FFFD81742-3882-46D7-9001-A32AF84914C2%2FFFD81742-3882-46D7-9001-A32AF84914C2.jpg' },
    { name: '鼓山', type: 'TEAM_MOVEMENT', url: 'https://teamweb.sporetrofit.com/Location/', area: '高雄市', postData: 'LID=TMEGS&LIDName=%E9%BC%93%E5%B1%B1%E9%81%8B%E5%8B%95%E4%B8%AD%E5%BF%83&CategoryImgURL=https%3A%2F%2Fbooking-tpsc.sporetrofit.com%3A8081%2FLOHASIcon%2FMenuItems%2FCS20240524%2F&address=804%E9%AB%98%E9%9B%84%E5%B8%82%E9%BC%93%E5%B1%B1%E5%8D%80%E6%A1%83%E5%AD%90%E5%9C%92%E8%B7%AF1%E8%99%9F&phone=%2807%29581-9888&businessHours=06%3A00%7E22%3A00&imageUrl=https%3A%2F%2Fresource.sporetrofit.com%2FWebAdmin%2Fimage%2FCS%2FLocation%2FTMEGS%2FLocationPic%2F22ECFDDE-E05F-4B85-8400-286158B2AD78%2F22ECFDDE-E05F-4B85-8400-286158B2AD78.jpg' },
    { name: '楠仔坑', type: 'TEAM_MOVEMENT', url: 'https://teamweb.sporetrofit.com/Location/', area: '高雄市', postData: 'LID=TMENZ&LIDName=%E6%A5%A0%E4%BB%94%E5%9D%91%E9%81%8B%E5%8B%95%E4%B8%AD%E5%BF%83&CategoryImgURL=https%3A%2F%2Fbooking-tpsc.sporetrofit.com%3A8081%2FLOHASIcon%2FMenuItems%2FCS20240524%2F&address=811%E9%AB%98%E9%9B%84%E5%B8%82%E6%A5%A0%E6%A2%93%E5%8D%80%E6%97%97%E6%A5%A0%E8%B7%AF168%E8%99%9F&phone=%2807%29353-6888&businessHours=06%3A00%7E22%3A00&imageUrl=https%3A%2F%2Fresource.sporetrofit.com%2FWebAdmin%2Fimage%2FCS%2FLocation%2FTMENZ%2FLocationPic%2FDABC04EC-5C1F-472C-9F38-8C8EBD6E111D%2FDABC04EC-5C1F-472C-9F38-8C8EBD6E111D.jpg' },
    { name: '嘉義', type: 'TEAM_MOVEMENT', url: 'https://teamweb.sporetrofit.com/Location/', area: '嘉義市', postData: 'LID=TMCY&LIDName=%E5%98%89%E7%BE%A9%E5%9C%8B%E6%B0%91%E9%81%8B%E5%8B%95%E4%B8%AD%E5%BF%83&CategoryImgURL=https%3A%2F%2Fbooking-tpsc.sporetrofit.com%3A8081%2FLOHASIcon%2FMenuItems%2FCS20240524%2F&address=600%E5%98%89%E7%BE%A9%E5%B8%82%E6%9D%B1%E5%8D%80%E5%BD%8C%E9%99%80%E8%B7%AF327%E5%B7%B715%E8%99%9F&phone=%2805%29216-5788&businessHours=06%3A00%7E22%3A00&imageUrl=https%3A%2F%2Fresource.sporetrofit.com%2FWebAdmin%2Fimage%2FCS%2FLocation%2FTMCY%2FLocationPic%2FCAD86699-0950-4DFB-AC9C-832C2139DBF0%2FCAD86699-0950-4DFB-AC9C-832C2139DBF0.jpg' },
    { name: '雲林縣', type: 'TEAM_MOVEMENT', url: 'https://teamweb.sporetrofit.com/Location/', area: '雲林縣', postData: 'LID=TMPYL&LIDName=%E9%9B%B2%E6%9E%97%E7%B8%A3%E5%9C%8B%E6%B0%91%E9%81%8B%E5%8B%95%E4%B8%AD%E5%BF%83&CategoryImgURL=https%3A%2F%2Fbooking-tpsc.sporetrofit.com%3A8081%2FLOHASIcon%2FMenuItems%2FCS20240524%2F&address=640%E9%9B%B2%E6%9E%97%E7%B8%A3%E6%96%97%E5%85%AD%E5%B8%82%E8%8E%8A%E6%95%AC%E8%B7%AF353%E8%99%9F&phone=%2805%29532-5888&businessHours=06%3A00%7E22%3A00&imageUrl=https%3A%2F%2Fresource.sporetrofit.com%2FWebAdmin%2Fimage%2FCS%2FLocation%2FTMPYL%2FLocationPic%2FAF060C88-11D8-466C-B777-338E5C549A7B%2FAF060C88-11D8-466C-B777-338E5C549A7B.jpg' },
    { name: '水上', type: 'TEAM_MOVEMENT', url: 'https://teamweb.sporetrofit.com/Location/', area: '嘉義縣', postData: 'LID=TMQSS&LIDName=%E6%B0%B4%E4%B8%8A%E5%85%A8%E6%B0%91%E9%81%8B%E5%8B%95%E9%A4%A8&CategoryImgURL=https%3A%2F%2Fbooking-tpsc.sporetrofit.com%3A8081%2FLOHASIcon%2FMenuItems%2FCS20240524%2F&address=%E5%98%89%E7%BE%A9%E7%B8%A3%E6%B0%B4%E4%B8%8A%E9%84%89%E6%9F%B3%E6%9E%97%E6%9D%911%E9%84%B0%E6%9F%B3%E5%AD%90%E6%9E%9780%E4%B9%8B2%E8%99%9F&phone=%2805%29268-9338&businessHours=06%3A00%7E22%3A00&imageUrl=https%3A%2F%2Fresource.sporetrofit.com%2FWebAdminStage%2Fimage%2FCS%2FLocation%2FTMQSS%2FLocationPic%2FAEFC2F14-6420-4BF9-A293-2DB687B23F24%2FAEFC2F14-6420-4BF9-A293-2DB687B23F24.jpg' },

];

// 【解析器】褆姆躍動事業體 HTML 解析
function parseTeamMovement(htmlText, name) {
  const extractData = (keyword) => {
    // 找出關鍵字（如「游泳池」或「健身房」）在 HTML 中的位置
    const idx = htmlText.indexOf(keyword);
    if (idx === -1) return null;

    // 只裁切關鍵字後方 400 個字元的 HTML 片段，絕不跨區抓到下方的地址與電話
    const block = htmlText.substring(idx, idx + 400);

    // 在這 400 字元內精準拿 col-3 (現在人數) 與 col-2 (容留人數)
    const currentMatch = block.match(/class=["'][^"']*col-3[^"']*["'][^>]*>\s*(\d+)\s*<\/div>/i);
    const maxMatch = block.match(/class=["'][^"']*col-2[^"']*["'][^>]*>\s*(\d+)\s*<\/div>/i);

    if (currentMatch && maxMatch) {
      return {
        current: parseInt(currentMatch[1], 10),
        max: parseInt(maxMatch[1], 10)
      };
    }
    return null;
  };

  const swimData = extractData('游泳池') || extractData('泳池');
  const gymData = extractData('健身房') || extractData('體適能');

  const swim = swimData ? { ...swimData, found: true } : { current: 0, max: 0, found: false };
  const gym = gymData ? { ...gymData, found: true } : { current: 0, max: 0, found: false };

  const isSuccess = swim.found || gym.found;

  return {
    name,
    gym: { current: gym.current, max: gym.max },
    swim: { current: swim.current, max: swim.max },
    status: isSuccess ? 'online' : 'offline',
    message: isSuccess ? null : '人數數據擷取失敗'
  };
}

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

// 【解析器】台北市運動中心 POST 聯合 API 處理（針對中正等館區）
function parseTPSC(data, name, targetLid) {
  const list = data?.locationPeopleNums || [];
  const target = list.find(item => item.LID === targetLid || item.lidName === name);

  if (!target) {
    return {
      name,
      gym: { current: 0, max: 0 },
      swim: { current: 0, max: 0 },
      status: 'offline',
      message: '未找到館區資料'
    };
  }

  return {
    name,
    gym: { 
      current: parseInt(target.gymPeopleNum, 10) || 0, 
      max: parseInt(target.gymMaxPeopleNum, 10) || 0 
    },
    swim: { 
      current: parseInt(target.swPeopleNum, 10) || 0, 
      max: parseInt(target.swMaxPeopleNum, 10) || 0 
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
      let response;

      if(center.type === 'TEAM_MOVEMENT')
      {
        // 褆姆躍動體系：發送 POST 請求與帶入表單資料
        response = await fetch(center.url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
          },
          body: center.postData
        });
      } else {
        response = await fetch(center.url, { 
          method: (center.type === 'TPSC_POST' || center.type === 'TEAM_MOVEMENT') ? 'POST' : 'GET',
          signal: controller.signal,
          headers: { 
            'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7'
          } 
        });
      }

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
      } else if (center.type === 'TPSC_POST') {
        const data = await response.json();
        parsedResult = parseTPSC(data, center.name, center.lid);
      } else if (center.type === 'TEAM_MOVEMENT') {
        // 👈 褆姆躍動網頁解析
        const htmlText = await response.text();
        console.log(`[連線日誌] ${center.name} 取得 :`, htmlText);
        parsedResult = parseTeamMovement(htmlText, center.name);
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