import SwiftUI

/// 「各區列表」與「我的最愛」共用的清單頁
struct CenterListView: View {
    let tab: AppTab

    @Environment(SportsCenterStore.self) private var store
    @Environment(LocationManager.self) private var locationManager

    private var centers: [SportsCenter] {
        store.displayedCenters(tab: tab, userLocation: locationManager.location)
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 16) {
                if let lastUpdated = store.lastUpdated {
                    HStack(spacing: 4) {
                        Spacer()
                        Image(systemName: "clock")
                            .font(.system(size: 12, weight: .semibold))
                        Text("更新：\(lastUpdated.formatted(date: .omitted, time: .standard))")
                            .font(.caption.weight(.bold))
                    }
                    .foregroundStyle(.primary.opacity(0.85))
                    .padding(.horizontal, 4)
                    .padding(.top, 8)
                }

                content
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 24)
            .frame(maxWidth: 600)
            .frame(maxWidth: .infinity)
        }
        .background(Theme.background)
        .refreshable { await store.fetchData() }
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                BrandTitle()
            }
            .sharedBackgroundVisibility(.hidden)

            ToolbarItem(placement: .topBarTrailing) {
                AreaMenu()
            }

            ToolbarItem(placement: .topBarTrailing) {
                RefreshButton()
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }

    @ViewBuilder
    private var content: some View {
        if store.loading && centers.isEmpty {
            ProgressView()
                .controlSize(.large)
                .tint(Theme.blue)
                .padding(.vertical, 48)
        } else if tab == .favorite && centers.isEmpty {
            EmptyFavoritesView()
        } else if centers.isEmpty, let error = store.errorMessage {
            ContentUnavailableView(error, systemImage: "wifi.exclamationmark", description: Text("請下拉或點擊右上角重新整理"))
                .padding(.top, 32)
        } else {
            ForEach(centers) { center in
                CenterCardView(center: center)
            }
        }
    }
}

/// 區域篩選下拉選單
struct AreaMenu: View {
    @Environment(SportsCenterStore.self) private var store

    var body: some View {
        @Bindable var store = store
        Menu {
            Picker("地區", selection: $store.selectedArea) {
                ForEach(store.areas, id: \.self) { area in
                    Text(area).tag(area)
                }
            }
        } label: {
            HStack(spacing: 4) {
                Text(store.selectedArea)
                    .font(.caption.weight(.bold))
                Image(systemName: "chevron.down")
                    .font(.system(size: 11, weight: .bold))
            }
            .foregroundStyle(.primary)
            .padding(.horizontal, 6)
        }
    }
}

/// 手動重整按鈕（載入中改顯示與下拉重整相同的系統 loading 圖示）
struct RefreshButton: View {
    @Environment(SportsCenterStore.self) private var store

    var body: some View {
        Button {
            Task { await store.fetchData() }
        } label: {
            if store.loading {
                ProgressView()
                    .controlSize(.small)
            } else {
                Image(systemName: "arrow.clockwise")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(Theme.blue)
            }
        }
        .disabled(store.loading)
        .accessibilityLabel("重新整理")
    }
}

struct EmptyFavoritesView: View {
    var body: some View {
        VStack(spacing: 8) {
            Image(systemName: "heart")
                .font(.system(size: 56))
                .foregroundStyle(Color(uiColor: .systemGray3))
                .padding(.bottom, 4)
            Text("尚無收藏的運動中心")
                .font(.title3.weight(.bold))
            Text("點擊卡片右上角愛心按鈕加入我的最愛")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 48)
        .padding(.horizontal, 24)
        .background(Theme.card, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
        .padding(.top, 16)
    }
}
