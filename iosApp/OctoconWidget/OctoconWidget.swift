import WidgetKit
import KeychainSwift
import SwiftUI

struct OctoconWidgetEntryView : View {
  var entry: OctoconWidgetProvider.Entry
  
  @Environment(\.widgetFamily) var family
  
  var body: some View {
    switch entry.state {
      case .data(let data):
        switch family {
          case .systemSmall: OctoconSmallWidget(data: data)
          case .systemMedium: OctoconMediumWidget(data: data)
          case .systemLarge: OctoconLargeWidget(data: data)
          default: Text("Widget size not supported.")
        }
      case .error(let error):
        let errorText = switch error {
          case .NOT_LOGGED_IN: "You must be logged in to use this widget."
          case .PIN_IS_PROTECTED: "Disable your Octocon PIN to use this widget."
          case .NETWORK_ERROR: "A network error occurred loading this widget."
        }
      
        Text(errorText)
    }
  }
}

struct OctoconFrontingWidget: Widget {
  let kind: String = "OctoconFrontingWidget"
  
  var body: some WidgetConfiguration {
    StaticConfiguration(kind: kind, provider: OctoconWidgetProvider()) { entry in
      if #available(iOS 17.0, *) {
        OctoconWidgetEntryView(entry: entry)
          .containerBackground(.fill.tertiary, for: .widget)
          .widgetAccentable()
      } else {
        OctoconWidgetEntryView(entry: entry)
          .padding()
          .background()
      }
    }
    .supportedFamilies([.systemSmall, .systemMedium, .systemLarge])
    .contentMarginsDisabled()
    .configurationDisplayName("Currently Fronting")
    .description("Displays your currently fronting alters.")
  }
}

#Preview(as: .systemSmall) {
  OctoconFrontingWidget()
} timeline: {
  generateDummyEntry()
}
