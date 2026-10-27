import SwiftUI

struct RootView: View {
    @Environment(SportsCenterStore.self) private var store
    @Environment(LocationManager.self) private var locationManager
    @Environment(\.scenePhase) private var scenePhase

    @State private var activeTab: AppTab = .list

    var body: some View {
        TabView(selection: $activeTab) {
            Tab("各區列表", systemImage: "list.bullet", value: AppTab.list) {
                NavigationStack {
                    CenterListView(tab: .list)
                }
            }

            Tab("我的最愛", systemImage: activeTab == .favorite ? "heart.fill" : "heart", value: AppTab.favorite) {
                NavigationStack {
                    CenterListView(tab: .favorite)
                }
            }

            Tab("公告&關於", systemImage: "megaphone", value: AppTab.about) {
                NavigationStack {
                    NoticeAboutView()
                }
            }
        }
        .task {
            // 縣市清單只會隨後端部署改變，啟動時讀一次即可
            await store.fetchAreas()
        }
        .task {
            locationManager.request()
            // 每 60 秒自動更新一次（與 webapp 相同）
            while !Task.isCancelled {
                await store.fetchData()
                try? await Task.sleep(for: .seconds(AppConfig.refreshInterval))
            }
        }
        .onOpenURL { url in
            // 從小工具點進來時切到「我的最愛」
            if url.host == AppConfig.favoritesDeepLink.host {
                activeTab = .favorite
            }
        }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                locationManager.request()
                Task { await store.fetchData() }
            }
        }
    }
}

/// 導覽列左側的品牌標題（Logo + 動潮 + 副標）
struct BrandTitle: View {
    var body: some View {
        HStack(spacing: 10) {
            Image("Logo")
                .resizable()
                .frame(width: 32, height: 32)
            VStack(alignment: .leading, spacing: 1) {
                Text("動潮")
                    .font(.system(size: 17, weight: .bold))
                    .tracking(-0.4)
                Text("台灣運動中心人潮")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(.secondary)
            }
        }
        .fixedSize()
    }
}

#Preview {
    RootView()
        .environment(SportsCenterStore())
        .environment(LocationManager())
}
