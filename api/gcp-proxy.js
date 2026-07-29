// 此檔案部署於 Gcp Cloud Run 當中呼叫
import { http } from '@google-cloud/functions-framework';
import axios from 'axios';

// ⚠️ 請直接填入你的 AWS IP（例如 '54.250.225.6'）
const ALLOWED_IP = '54.250.225.6';

http('helloHttp', async (req, res) => {
  // CORS
  res.set('Access-Control-Allow-Origin', '*');
  res.set('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.set('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    return res.status(204).send('');
  }

  // 取得真實來源 IP
  const xForwardedFor = req.headers['x-forwarded-for'] || '';
  const clientIp = xForwardedFor ? xForwardedFor.split(',')[0].trim() : req.socket.remoteAddress;

  // IP 檢查
  if (ALLOWED_IP && ALLOWED_IP !== '你的_AWS_IP_寫在這裡' && clientIp !== ALLOWED_IP) {
    console.warn(`[攔截] 非授權 IP 試圖存取: ${clientIp}`);
    return res.status(403).json({
      error: 'Forbidden',
      message: `拒絕存取：您的 IP (${clientIp}) 不在允許清單中`
    });
  }

  // 目轉網址代理
  const targetUrl = req.query.url;
  if (!targetUrl) {
    return res.status(400).send('錯誤：請提供目標 URL 參數，例如 ?url=https://...');
  }

  try {
    const response = await axios.get(targetUrl, {
      headers: {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'application/json, text/plain, */*'
      },
      timeout: 8000
    });

    return res.status(200).json(response.data);
  } catch (error) {
    console.error('Proxy 轉發失敗:', error.message);
    return res.status(error.response?.status || 500).json({
      error: 'Proxy 轉發失敗',
      message: error.message
    });
  }
});