import CoreLocation
import Foundation
import Observation
import WidgetKit

enum AppTab: Hashable {
    case list, favorite, about
}

@MainActor
@Observable
final class SportsCenterStore {
    private(set) var centers: [SportsCenter] = []
    private(set) var loading = false
    private(set) var lastUpdated: Date?
    private(set) var errorMessage: String?
    private(set) var favorites: [String] = []
    /// 由 API /areas 取得的縣市清單；尚未取得時為 nil
    private var apiAreas: [String]?

    var selectedArea = AppConfig.allAreas

    /// 篩選選單的選項：「全部」+ API 定義的縣市
    var areas: [String] {
        // /areas 尚未載入或失敗時，改由運動中心資料的 area 依出現順序推導
        var seen = Set<String>()
        let list = apiAreas ?? centers.map(\.area).filter { seen.insert($0).inserted }
        return [AppConfig.allAreas] + list
    }

    init() {
        favorites = SharedStorage.favorites
        // 先顯示上次的快取，避免冷啟動時等待 API
        if let cache = SharedStorage.loadCenters() {
            centers = cache.centers
            lastUpdated = cache.date
        }
    }

    // MARK: - 資料抓取

    func fetchData() async {
        guard !loading else { return }
        loading = true
        defer { loading = false }

        do {
            centers = try await SharedStorage.fetchCenters()
            lastUpdated = Date()
            errorMessage = nil
            // 寫入共用快取並通知小工具更新
            SharedStorage.saveCenters(centers)
            WidgetCenter.shared.reloadAllTimelines()
        } catch {
            print("無法取得運動中心資料:", error)
            errorMessage = "無法取得運動中心資料"
        }
    }

    func fetchAreas() async {
        do {
            apiAreas = try await SharedStorage.fetchAreas()
        } catch {
            print("無法取得縣市清單:", error)
        }
        // 目前選取的縣市不在新清單中時，回到「全部」
        if !areas.contains(selectedArea) {
            selectedArea = AppConfig.allAreas
        }
    }

    // MARK: - 我的最愛（與 webapp 相同 key，存在 App Group，小工具也會讀取）

    func isFavorite(_ name: String) -> Bool {
        favorites.contains(name)
    }

    func toggleFavorite(_ name: String) {
        if isFavorite(name) {
            favorites.removeAll { $0 == name }
        } else {
            favorites.append(name)
        }
        SharedStorage.favorites = favorites
        WidgetCenter.shared.reloadAllTimelines()
    }

    // MARK: - 顯示清單（距離排序 → 最愛篩選 → 地區篩選）

    func displayedCenters(tab: AppTab, userLocation: CLLocation?) -> [SportsCenter] {
        var list = centers

        if let userLocation {
            list = list.map { center in
                var c = center
                if let lat = center.lat, let lng = center.lng {
                    c.distance = userLocation.distance(from: CLLocation(latitude: lat, longitude: lng)) / 1000
                }
                return c
            }
            list.sort { a, b in
                switch (a.distance, b.distance) {
                case let (x?, y?): return x < y
                case (nil, _): return false
                case (_, nil): return true
                }
            }
        }

        if tab == .favorite {
            list = list.filter { isFavorite($0.name) }
        }

        if selectedArea != AppConfig.allAreas {
            list = list.filter { $0.area == selectedArea }
        }

        return list
    }
}
