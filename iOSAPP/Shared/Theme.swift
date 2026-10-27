import SwiftUI

/// 對應 webapp 的 iOS 風格配色
enum Theme {
    static let blue = Color(red: 0x00 / 255, green: 0x7B / 255, blue: 0xBF / 255)   // #007BBF
    static let orange = Color(red: 0xFF / 255, green: 0x95 / 255, blue: 0x00 / 255) // #FF9500
    static let red = Color(red: 0xFF / 255, green: 0x3B / 255, blue: 0x30 / 255)    // #FF3B30
    static let gray = Color(red: 0x8E / 255, green: 0x8E / 255, blue: 0x93 / 255)   // #8E8E93

    static let background = Color(uiColor: .systemGroupedBackground)       // #f2f2f7 / #000000
    static let card = Color(uiColor: .secondarySystemGroupedBackground)    // #ffffff / #1c1c1e
    static let chip = Color(uiColor: .systemGray5)                         // #e5e5ea / #2c2c2e
    static let divider = Color(uiColor: .separator)

    /// 依人數比例回傳顏色：<50% 藍、50~80% 橘、>80% 紅，無容留上限則灰
    static func progressColor(current: Int, max: Int) -> Color {
        guard max > 0 else { return gray }
        let ratio = Double(current) / Double(max) * 100
        if ratio < 50 { return blue }
        if ratio <= 80 { return orange }
        return red
    }
}
