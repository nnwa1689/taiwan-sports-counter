import Foundation

enum AppConfig {
    /// 與 webapp 的 VITE_API_URL 相同；可在 Info.plist 的 `API_URL` 覆寫
    static var apiURL: URL {
        if let value = Bundle.main.object(forInfoDictionaryKey: "API_URL") as? String,
           !value.isEmpty, let url = URL(string: value) {
            return url
        }
        return URL(string: "https://teddream.tw/api/sports-centers")!
    }

    /// 自動更新間隔（秒）
    static let refreshInterval: TimeInterval = 60

    /// 縣市清單端點，與 sports-centers 同一層路徑（例如 /api/areas）
    static var areasURL: URL {
        URL(string: "areas", relativeTo: apiURL)!.absoluteURL
    }

    /// 縣市篩選的「不篩選」選項（用戶端自己的概念，不由 API 提供）
    static let allAreas = "全部"

    static let favoritesKey = "sports_center_favorites"

    /// App 與小工具共用資料的 App Group
    static let appGroupID = "group.tw.teddream.TaiwanSportsCounter"

    /// 小工具點擊後開啟「我的最愛」分頁
    static let favoritesDeepLink = URL(string: "dongchao://favorite")!

    static var appVersion: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
    }
}
