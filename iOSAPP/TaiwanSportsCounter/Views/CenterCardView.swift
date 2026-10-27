import SwiftUI

struct CenterCardView: View {
    let center: SportsCenter

    @Environment(SportsCenterStore.self) private var store

    var body: some View {
        VStack(spacing: 0) {
            header
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 12)

            Divider()

            if center.isOnline {
                VStack(spacing: 16) {
                    FacilityRow(title: "健身房", systemImage: "dumbbell.fill", facility: center.gym ?? Facility(current: 0, max: 0))
                    FacilityRow(title: "游泳池", systemImage: "figure.pool.swim", facility: center.swim ?? Facility(current: 0, max: 0))
                }
                .padding(16)
            } else {
                VStack(spacing: 4) {
                    Image(systemName: "exclamationmark.circle")
                        .foregroundStyle(Theme.red)
                    Text("資料載入失敗\(center.message.map { " (\($0))" } ?? "")")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
            }
        }
        .background(Theme.card, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private var header: some View {
        HStack {
            Text("\(center.name)運動中心")
                .font(.headline.weight(.bold))
                .lineLimit(1)

            Text(chipText)
                .font(.system(size: 11, weight: .bold))
                .foregroundStyle(Theme.blue)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(Theme.chip, in: RoundedRectangle(cornerRadius: 8, style: .continuous))

            Spacer(minLength: 8)

            let favorite = store.isFavorite(center.name)
            Button {
                withAnimation(.spring(duration: 0.25)) {
                    store.toggleFavorite(center.name)
                }
            } label: {
                Image(systemName: favorite ? "heart.fill" : "heart")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(favorite ? Theme.red : .secondary)
                    .contentTransition(.symbolEffect(.replace))
                    .frame(width: 34, height: 34)
                    .background(Color.primary.opacity(0.04), in: Circle())
                    .overlay(Circle().stroke(Color.primary.opacity(0.06), lineWidth: 1))
            }
            .buttonStyle(.plain)
            .sensoryFeedback(.selection, trigger: favorite)
            .accessibilityLabel(favorite ? "移除最愛" : "加入最愛")
        }
    }

    private var chipText: String {
        if let distance = center.distance {
            return "\(center.area) • \(String(format: "%.1f", distance)) km"
        }
        return center.area
    }
}

struct FacilityRow: View {
    let title: String
    let systemImage: String
    let facility: Facility

    private var color: Color {
        Theme.progressColor(current: facility.current, max: facility.max)
    }

    var body: some View {
        VStack(spacing: 6) {
            HStack(alignment: .firstTextBaseline) {
                Label {
                    Text(title).font(.body.weight(.bold))
                } icon: {
                    Image(systemName: systemImage).foregroundStyle(color)
                }
                Spacer()
                Text("\(facility.current)")
                    .font(.title3.weight(.black))
                    .foregroundStyle(color)
                    .contentTransition(.numericText())
                Text("/ \(facility.max) 人")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(.secondary)
            }

            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule().fill(Theme.chip)
                    Capsule()
                        .fill(color)
                        .frame(width: geo.size.width * facility.ratio)
                }
            }
            .frame(height: 8)
            .animation(.easeInOut(duration: 0.4), value: facility.ratio)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(title) \(facility.current) 人，容留 \(facility.max) 人")
    }
}
