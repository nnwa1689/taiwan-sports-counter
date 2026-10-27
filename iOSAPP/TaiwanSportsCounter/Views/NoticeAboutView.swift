import SwiftUI

struct Notice: Identifiable {
    let id = UUID()
    let title: String
    let date: String
    let content: String
}

struct NoticeAboutView: View {
    private let notices: [Notice] = [
        Notice(title: "新增主畫面小工具", date: "2026-10-27", content: "可在主畫面或鎖定畫面加入「我的最愛」小工具，不用開 App 就能看到收藏館的人數。"),
        Notice(title: "推出 iOS App", date: "2026-10-27", content: "動潮現在有原生 iOS App 了，功能與網頁版相同。"),
        Notice(title: "新增距離排序功能", date: "2026-08-15", content: "現在中心清單將以距離排序（如果你同意提供位置）。"),
        Notice(title: "新增桌面版UI", date: "2026-08-01", content: "調整了一下桌面版介面。"),
        Notice(title: "已修復無法顯示的區域", date: "2026-07-28", content: "已修復全部的運動中心，並調整了一下介面。"),
        Notice(title: "視覺風格更新", date: "2026-07-28", content: "新增嘉義、雲林等區，高雄新增楠梓、鼓山。"),
        Notice(title: "新增區域篩選與我的最愛", date: "2026-07-27", content: "現在您可以跨縣市篩選運動中心，並透過愛心按鈕將常用的館區加入「我的最愛」快顯清單。"),
    ]

    private let techs = ["SwiftUI", "Observation", "CoreLocation", "URLSession", "REST API"]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                noticeCard
                aboutCard
                DataSourceCard()
            }
            .padding(16)
            .frame(maxWidth: 600)
            .frame(maxWidth: .infinity)
        }
        .background(Theme.background)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                BrandTitle()
            }
            .sharedBackgroundVisibility(.hidden)
        }
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - 最新公告

    private var noticeCard: some View {
        SectionCard(title: "最新公告", systemImage: "megaphone") {
            VStack(alignment: .leading, spacing: 0) {
                ForEach(Array(notices.enumerated()), id: \.element.id) { index, notice in
                    HStack(alignment: .top, spacing: 12) {
                        // 時間軸圓點與連接線
                        VStack(spacing: 0) {
                            Circle()
                                .fill(Theme.blue)
                                .frame(width: 10, height: 10)
                                .padding(.top, 5)
                            if index < notices.count - 1 {
                                Rectangle()
                                    .fill(Theme.divider)
                                    .frame(width: 1.5)
                            }
                        }
                        .frame(width: 10)

                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text(notice.title).font(.subheadline.weight(.bold))
                                Spacer()
                                Text(notice.date)
                                    .font(.caption.weight(.medium))
                                    .foregroundStyle(.secondary)
                            }
                            Text(notice.content)
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                                .lineSpacing(3)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                        .padding(.bottom, 16)
                    }
                }
            }
        }
    }

    // MARK: - 關於本站

    private var aboutCard: some View {
        SectionCard(title: "關於本站", systemImage: "info.circle") {
            VStack(alignment: .leading, spacing: 16) {
                HStack(spacing: 12) {
                    Image("Logo")
                        .resizable()
                        .frame(width: 48, height: 48)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("動潮｜台灣運動中心即時人潮")
                            .font(.subheadline.weight(.bold))
                        Text("版本 v\(AppConfig.appVersion)")
                            .font(.caption.weight(.medium))
                            .foregroundStyle(.secondary)
                    }
                    Spacer(minLength: 0)
                }
                .padding(12)
                .background(Theme.background, in: RoundedRectangle(cornerRadius: 14, style: .continuous))

                Text("本平台提供各縣市運動中心健身房與游泳池之即時人潮數據查詢，協助您避開擁擠高峰期，享有更好的運動體驗。數據每分鐘自動更新。")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineSpacing(3)

                Divider()

                VStack(alignment: .leading, spacing: 8) {
                    Text("使用技術與架構")
                        .font(.caption.weight(.bold))
                        .foregroundStyle(.secondary)
                    FlowLayout(spacing: 6) {
                        ForEach(techs, id: \.self) { tech in
                            Text(tech)
                                .font(.caption.weight(.semibold))
                                .foregroundStyle(Theme.blue)
                                .padding(.horizontal, 10)
                                .padding(.vertical, 5)
                                .background(Theme.background, in: Capsule())
                        }
                    }
                }

                VStack(spacing: 2) {
                    HStack(spacing: 4) {
                        Text("Designed & Developed by")
                        Link("TEDDREAM", destination: URL(string: "https://teddream.tw")!)
                            .fontWeight(.bold)
                    }
                    Text("© 2026 All Rights Reserved.")
                }
                .font(.caption)
                .foregroundStyle(.secondary)
                .frame(maxWidth: .infinity)
                .padding(.top, 4)
            }
        }
    }
}

/// 資料來源與非官方聲明
struct DataSourceCard: View {
    var body: some View {
        SectionCard(title: "資料來源與聲明", systemImage: "doc.text.magnifyingglass") {
            VStack(alignment: .leading, spacing: 12) {
                Label {
                    Text("本 App 為個人開發的非官方服務，與各運動中心及其營運單位無隸屬或合作關係。")
                } icon: {
                    Image(systemName: "exclamationmark.shield").foregroundStyle(Theme.orange)
                }
                .font(.subheadline.weight(.semibold))

                Text("人數資料取自各運動中心官方網站公開之即時資訊，經整理後顯示，約每分鐘更新一次，可能有延遲或誤差，實際狀況請以現場公告為準。")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .lineSpacing(3)
            }
        }
    }
}

/// 帶標題與分隔線的卡片
struct SectionCard<Content: View>: View {
    let title: String
    let systemImage: String
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Label {
                Text(title).font(.headline.weight(.bold))
            } icon: {
                Image(systemName: systemImage).foregroundStyle(Theme.blue)
            }
            .padding(16)

            Divider()

            content
                .padding(16)
        }
        .background(Theme.card, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

/// 簡易自動換行排版（技術標籤用）
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var x: CGFloat = 0, y: CGFloat = 0, rowHeight: CGFloat = 0, width: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > maxWidth, x > 0 {
                x = 0
                y += rowHeight + spacing
                rowHeight = 0
            }
            x += size.width + spacing
            width = max(width, x - spacing)
            rowHeight = max(rowHeight, size.height)
        }
        return CGSize(width: width, height: y + rowHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX, y = bounds.minY, rowHeight: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX, x > bounds.minX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            view.place(at: CGPoint(x: x, y: y), proposal: ProposedViewSize(size))
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
