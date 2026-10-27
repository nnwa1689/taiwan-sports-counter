import Foundation

/// 單一設施（健身房 / 游泳池）的人數
struct Facility: Codable, Hashable {
    let current: Int
    let max: Int

    enum CodingKeys: String, CodingKey { case current, max }

    init(current: Int, max: Int) {
        self.current = current
        self.max = max
    }

    // 後端 parseInt 失敗時會回傳 null，這裡寬鬆解析為 0
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        current = (try? c.decodeIfPresent(Int.self, forKey: .current)) ?? 0
        max = (try? c.decodeIfPresent(Int.self, forKey: .max)) ?? 0
    }

    /// 0...1 的使用比例
    var ratio: Double {
        guard max > 0 else { return 0 }
        return min(Double(current) / Double(max), 1)
    }
}

/// 對應後端 `/sports-centers` 回傳的單筆運動中心資料
struct SportsCenter: Codable, Identifiable, Hashable {
    let name: String
    let area: String
    let lat: Double?
    let lng: Double?
    let status: String
    let message: String?
    let gym: Facility?
    let swim: Facility?

    /// 由使用者位置計算出的距離（公里），不由 API 提供
    var distance: Double? = nil

    var id: String { name }
    var isOnline: Bool { status == "online" }

    enum CodingKeys: String, CodingKey {
        case name, area, lat, lng, status, message, gym, swim
    }
}
