![ICON](https://raw.githubusercontent.com/nnwa1689/taiwan-sports-counter/refs/heads/main/webapp/taiwan-sports-counter/public/icon-192.png)
## API
`node server.js`
API負責呼叫抓取資料
## PROXY
部署到 Google Cloud Run，使用台灣IP發出請求，取得資料
## WebAPP
`npm run dev`
VueJS 的 APP，已設定對應設定檔 VITE_API_URL
