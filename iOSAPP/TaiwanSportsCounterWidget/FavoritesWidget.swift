import SwiftUI
import WidgetKit

@main
struct TaiwanSportsCounterWidgetBundle: WidgetBundle {
    var body: some Widget {
        FavoritesWidget()
    }
}

/// 「我的最愛」小工具：顯示收藏運動中心的健身房／游泳池即時人數
struct FavoritesWidget: Widget {
    let kind = "FavoritesWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: FavoritesProvider()) { entry in
            FavoritesWidgetView(entry: entry)
                .containerBackground(Theme.card, for: .widget)
                .widgetURL(AppConfig.favoritesDeepLink)
        }
        .configurationDisplayName("我的最愛")
        .description("在主畫面查看收藏運動中心的即時人潮。")
        .supportedFamilies([.systemSmall, .systemMedium, .systemLarge, .accessoryRectangular, .accessoryInline])
    }
}

// MARK: - Timeline

struct FavoritesEntry: TimelineEntry {
    let date: Date
    /// 依加入最愛的順序排列
    let centers: [SportsCenter]
    let hasFavorites: Bool
    let updatedAt: Date?

    static let sample = FavoritesEntry(
        date: .now,
        centers: [
            SportsCenter(name: "中正", area: "台北市", lat: nil, lng: nil, status: "online", message: nil,
                         gym: Facility(current: 102, max: 160), swim: Facility(current: 44, max: 200)),
            SportsCenter(name: "大同", area: "台北市", lat: nil, lng: nil, status: "online", message: nil,
                         gym: Facility(current: 72, max: 80), swim: Facility(current: 76, max: 250)),
            SportsCenter(name: "三重", area: "新北市", lat: nil, lng: nil, status: "online", message: nil,
                         gym: Facility(current: 47, max: 60), swim: Facility(current: 36, max: 400)),
        ],
        hasFavorites: true,
        updatedAt: .now
    )
}

struct FavoritesProvider: TimelineProvider {
    /// 小工具大約每 15 分鐘更新一次（實際頻率由系統決定）
    private let refreshInterval: TimeInterval = 15 * 60

    /// 共用快取在這個時間內視為新鮮，不重新呼叫 API
    private let maxCacheAge: TimeInterval = 5 * 60

    func placeholder(in context: Context) -> FavoritesEntry {
        .sample
    }

    func getSnapshot(in context: Context, completion: @escaping (FavoritesEntry) -> Void) {
        let entry = cachedEntry()
        if context.isPreview && (entry.centers.isEmpty || !entry.hasFavorites) {
            completion(.sample)
        } else {
            completion(entry)
        }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<FavoritesEntry>) -> Void) {
        // WidgetKit 允許在任意執行緒呼叫 completion
        nonisolated(unsafe) let completion = completion
        Task {
            // 快取夠新（例如 App 開著、每 60 秒剛寫入）就不連網，避免重複呼叫 API；
            // 過期才抓最新資料，失敗時沿用舊快取
            if !isCacheFresh(), let centers = try? await SharedStorage.fetchCenters(timeout: 20) {
                SharedStorage.saveCenters(centers)
            }
            let entry = cachedEntry()
            completion(Timeline(entries: [entry], policy: .after(.now.addingTimeInterval(refreshInterval))))
        }
    }

    private func isCacheFresh() -> Bool {
        guard let cache = SharedStorage.loadCenters() else { return false }
        return Date().timeIntervalSince(cache.date) < maxCacheAge
    }

    private func cachedEntry() -> FavoritesEntry {
        let favorites = SharedStorage.favorites
        let cache = SharedStorage.loadCenters()
        let byName = Dictionary((cache?.centers ?? []).map { ($0.name, $0) }, uniquingKeysWith: { a, _ in a })
        return FavoritesEntry(
            date: .now,
            centers: favorites.compactMap { byName[$0] },
            hasFavorites: !favorites.isEmpty,
            updatedAt: cache?.date
        )
    }
}

// MARK: - Views

struct FavoritesWidgetView: View {
    let entry: FavoritesEntry

    @Environment(\.widgetFamily) private var family

    var body: some View {
        if !entry.hasFavorites {
            EmptyFavoritesWidgetView(compact: family == .accessoryRectangular || family == .accessoryInline)
        } else if entry.centers.isEmpty {
            MessageWidgetView(title: "尚無資料", message: "開啟 App 更新人數")
        } else {
            switch family {
            case .systemSmall:
                SmallWidgetView(center: entry.centers[0], updatedAt: entry.updatedAt)
            case .systemMedium:
                MediumWidgetView(centers: Array(entry.centers.prefix(2)), updatedAt: entry.updatedAt)
            case .systemLarge:
                LargeWidgetView(centers: Array(entry.centers.prefix(6)), updatedAt: entry.updatedAt)
            case .accessoryRectangular:
                RectangularWidgetView(center: entry.centers[0])
            case .accessoryInline:
                InlineWidgetView(center: entry.centers[0])
            default:
                SmallWidgetView(center: entry.centers[0], updatedAt: entry.updatedAt)
            }
        }
    }
}

/// 單一設施：圖示、人數與進度條
struct WidgetFacilityView: View {
    let systemImage: String
    let facility: Facility?

    private var color: Color {
        guard let facility else { return Theme.gray }
        return Theme.progressColor(current: facility.current, max: facility.max)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .firstTextBaseline, spacing: 4) {
                Image(systemName: systemImage)
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundStyle(color)
                    .frame(width: 18, alignment: .leading)
                Text("\(facility?.current ?? 0)")
                    .font(.system(size: 20, weight: .black, design: .rounded))
                    .foregroundStyle(color)
                    .contentTransition(.numericText())
                Text("/ \(facility?.max ?? 0)")
                    .font(.system(size: 11, weight: .semibold))
                    .foregroundStyle(.secondary)
                Spacer(minLength: 0)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule().fill(Theme.chip)
                    Capsule()
                        .fill(color)
                        .frame(width: geo.size.width * (facility?.ratio ?? 0))
                }
            }
            .frame(height: 6)
        }
    }
}

/// 運動中心區塊：名稱 + 健身房 + 游泳池（小型、中型共用）
struct WidgetCenterBlock: View {
    let center: SportsCenter

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("\(center.name)運動中心")
                .font(.system(size: 14, weight: .bold))
                .lineLimit(1)
                .minimumScaleFactor(0.8)

            if center.isOnline {
                WidgetFacilityView(systemImage: "dumbbell.fill", facility: center.gym)
                WidgetFacilityView(systemImage: "figure.pool.swim", facility: center.swim)
            } else {
                Spacer(minLength: 0)
                Label("資料暫無法取得", systemImage: "exclamationmark.circle")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                Spacer(minLength: 0)
            }
        }
    }
}

struct UpdatedAtView: View {
    let date: Date?

    /// 小工具空間有限，固定用 24 小時制「HH:mm」
    private static let formatter: DateFormatter = {
        let f = DateFormatter()
        f.locale = Locale(identifier: "en_US_POSIX")
        f.dateFormat = "HH:mm"
        return f
    }()

    var body: some View {
        if let date {
            HStack(spacing: 2) {
                Image(systemName: "clock")
                Text(verbatim: Self.formatter.string(from: date))
            }
            .font(.system(size: 10, weight: .semibold))
            .foregroundStyle(.secondary)
        }
    }
}

struct SmallWidgetView: View {
    let center: SportsCenter
    let updatedAt: Date?

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            WidgetCenterBlock(center: center)
            Spacer(minLength: 4)
            HStack {
                Text(center.area)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(Theme.blue)
                Spacer()
                UpdatedAtView(date: updatedAt)
            }
        }
    }
}

struct WidgetHeader: View {
    let updatedAt: Date?

    var body: some View {
        HStack(spacing: 6) {
            Image("Logo")
                .resizable()
                .frame(width: 18, height: 18)
                .clipShape(RoundedRectangle(cornerRadius: 4, style: .continuous))
            Text("我的最愛")
                .font(.system(size: 13, weight: .bold))
            Spacer()
            UpdatedAtView(date: updatedAt)
        }
    }
}

struct MediumWidgetView: View {
    let centers: [SportsCenter]
    let updatedAt: Date?

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            WidgetHeader(updatedAt: updatedAt)
            HStack(alignment: .top, spacing: 16) {
                ForEach(centers) { center in
                    WidgetCenterBlock(center: center)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
                if centers.count == 1 {
                    Color.clear.frame(maxWidth: .infinity)
                }
            }
            Spacer(minLength: 0)
        }
    }
}

struct LargeWidgetView: View {
    let centers: [SportsCenter]
    let updatedAt: Date?

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            WidgetHeader(updatedAt: updatedAt)
                .padding(.bottom, 8)
            ForEach(Array(centers.enumerated()), id: \.element.id) { index, center in
                if index > 0 {
                    Divider().padding(.vertical, 6)
                }
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("\(center.name)運動中心")
                            .font(.system(size: 14, weight: .bold))
                            .lineLimit(1)
                        Text(center.area)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundStyle(Theme.blue)
                    }
                    .frame(width: 96, alignment: .leading)

                    if center.isOnline {
                        WidgetFacilityView(systemImage: "dumbbell.fill", facility: center.gym)
                        WidgetFacilityView(systemImage: "figure.pool.swim", facility: center.swim)
                    } else {
                        Text("資料暫無法取得")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                }
            }
            Spacer(minLength: 0)
            if centers.count < 6 {
                Label("在 App 中點擊愛心加入更多運動中心", systemImage: "heart")
                    .font(.system(size: 11, weight: .medium))
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity)
            }
        }
    }
}

/// 鎖定畫面（矩形）
struct RectangularWidgetView: View {
    let center: SportsCenter

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("\(center.name)運動中心")
                .font(.headline)
                .widgetAccentable()
            if center.isOnline {
                Label("\(center.gym?.current ?? 0) / \(center.gym?.max ?? 0)", systemImage: "dumbbell.fill")
                Label("\(center.swim?.current ?? 0) / \(center.swim?.max ?? 0)", systemImage: "figure.pool.swim")
            } else {
                Text("資料暫無法取得")
            }
        }
        .font(.caption)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// 鎖定畫面（單行，時間上方）
struct InlineWidgetView: View {
    let center: SportsCenter

    var body: some View {
        if center.isOnline {
            Label("\(center.name) 健身房 \(center.gym?.current ?? 0)/\(center.gym?.max ?? 0)", systemImage: "dumbbell.fill")
        } else {
            Text("\(center.name) 資料暫無法取得")
        }
    }
}

struct EmptyFavoritesWidgetView: View {
    let compact: Bool

    var body: some View {
        if compact {
            Label("尚無我的最愛", systemImage: "heart")
        } else {
            VStack(spacing: 6) {
                Image(systemName: "heart")
                    .font(.system(size: 28))
                    .foregroundStyle(Color(uiColor: .systemGray3))
                Text("尚無我的最愛")
                    .font(.system(size: 14, weight: .bold))
                Text("在 App 中點擊愛心加入")
                    .font(.system(size: 11))
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

struct MessageWidgetView: View {
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: 4) {
            Text(title).font(.system(size: 14, weight: .bold))
            Text(message)
                .font(.system(size: 11))
                .foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

#Preview(as: .systemSmall) {
    FavoritesWidget()
} timeline: {
    FavoritesEntry.sample
}

#Preview(as: .systemMedium) {
    FavoritesWidget()
} timeline: {
    FavoritesEntry.sample
}

#Preview(as: .systemLarge) {
    FavoritesWidget()
} timeline: {
    FavoritesEntry.sample
}
