import Foundation

/// App 與小工具共用的儲存區（App Group UserDefaults）
enum SharedStorage {
    private static let centersKey = "cached_centers"
    private static let cachedAtKey = "cached_centers_at"

    static var defaults: UserDefaults {
        UserDefaults(suiteName: AppConfig.appGroupID) ?? .standard
    }

    // MARK: - 我的最愛（JSON 字串，格式與 webapp 的 localStorage 相同）

    static var favorites: [String] {
        get {
            // 舊版存在 standard，第一次讀取時搬到 App Group
            if defaults.string(forKey: AppConfig.favoritesKey) == nil,
               let legacy = UserDefaults.standard.string(forKey: AppConfig.favoritesKey) {
                defaults.set(legacy, forKey: AppConfig.favoritesKey)
            }
            guard let saved = defaults.string(forKey: AppConfig.favoritesKey),
                  let data = saved.data(using: .utf8),
                  let list = try? JSONDecoder().decode([String].self, from: data) else {
                return []
            }
            return list
        }
        set {
            if let data = try? JSONEncoder().encode(newValue), let json = String(data: data, encoding: .utf8) {
                defaults.set(json, forKey: AppConfig.favoritesKey)
            }
        }
    }

    // MARK: - 最近一次成功取得的資料（小工具連線失敗時使用）

    static func saveCenters(_ centers: [SportsCenter], at date: Date = Date()) {
        guard let data = try? JSONEncoder().encode(centers) else { return }
        defaults.set(data, forKey: centersKey)
        defaults.set(date, forKey: cachedAtKey)
    }

    static func loadCenters() -> (centers: [SportsCenter], date: Date)? {
        guard let data = defaults.data(forKey: centersKey),
              let centers = try? JSONDecoder().decode([SportsCenter].self, from: data),
              let date = defaults.object(forKey: cachedAtKey) as? Date else {
            return nil
        }
        return (centers, date)
    }

    // MARK: - 網路

    static func fetchCenters(timeout: TimeInterval = 30) async throws -> [SportsCenter] {
        try await fetchJSON([SportsCenter].self, from: AppConfig.apiURL, timeout: timeout)
    }

    static func fetchAreas(timeout: TimeInterval = 15) async throws -> [String] {
        try await fetchJSON([String].self, from: AppConfig.areasURL, timeout: timeout)
    }

    private static func fetchJSON<T: Decodable>(_ type: T.Type, from url: URL, timeout: TimeInterval) async throws -> T {
        var request = URLRequest(url: url)
        request.timeoutInterval = timeout
        request.cachePolicy = .reloadIgnoringLocalCacheData
        let (data, response) = try await URLSession.shared.data(for: request)
        if let http = response as? HTTPURLResponse, !(200..<300).contains(http.statusCode) {
            throw URLError(.badServerResponse)
        }
        return try JSONDecoder().decode(T.self, from: data)
    }
}
