import SwiftUI

@main
struct TaiwanSportsCounterApp: App {
    @State private var store = SportsCenterStore()
    @State private var locationManager = LocationManager()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(store)
                .environment(locationManager)
                .tint(Theme.blue)
        }
    }
}
