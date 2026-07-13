// xinyi-json-dumper.js
process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';

async function dumpXinyiJson() {
  console.log(`\n==================== [信義] JSON 骨架全展開 ====================`);
  try {
    const res = await fetch('https://xysc.teamxports.com/', {
      headers: { 'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)' }
    });
    const html = await res.text();
    const match = html.match(/<script id="__NEXT_DATA__" type="application\/json">([\s\S]*?)<\/script>/);
    
    if (match) {
      const parsed = JSON.parse(match[1]);
      
      // 1. 來看它的第一層結構到底長怎樣
      console.log("【第一層大類】:", Object.keys(parsed));
      if (parsed.props) console.log("【props 子類】:", Object.keys(parsed.props));
      if (parsed.props?.pageProps) console.log("【pageProps 子類】:", Object.keys(parsed.props.pageProps));
      
      // 2. 暴力印出所有「包含數字且不是陣列」的欄位對稱，把隱藏的 current / max 揪出來
      console.log("\n【全 JSON 鍵值對地毯式掃描】(過濾掉長文章與非必要欄位)：");
      
      function dumpAllPairs(obj, depth = 0) {
        if (!obj || depth > 7) return;
        if (typeof obj === 'object') {
          for (const key in obj) {
            const val = obj[key];
            if (typeof val === 'number' || (typeof val === 'string' && val.length < 20)) {
              // 印出看起來像跟人數、ID、容量相關的 key
              if (['num', 'count', 'max', 'limit', 'qty', 'id', 'total', 'now', 'space', 'current'].some(k => key.toLowerCase().includes(k)) || typeof val === 'number') {
                console.log(`${'  '.repeat(depth)}🔑 ${key}: ${val}`);
              }
            }
            dumpAllPairs(val, depth + 1);
          }
        }
      }
      
      dumpAllPairs(parsed.props);
    } else {
      console.log("找不到 __NEXT_DATA__");
    }
  } catch (e) { console.error(e.message); }
}

dumpXinyiJson();