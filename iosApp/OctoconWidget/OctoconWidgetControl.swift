import AppIntents
import SwiftUI
import WidgetKit

@available(iOS 18.0, *)
struct OctoconWidgetControl: ControlWidget {
  var body: some ControlWidgetConfiguration {
  StaticControlConfiguration(
    kind: "app.octocon.OctoconApp.OctoconWidget",
    provider: Provider()
  ) { value in
    ControlWidgetToggle(
      "Start Timer",
      isOn: value,
      action: StartTimerIntent()
    ) { isRunning in
      Label(isRunning ? "On" : "Off", systemImage: "timer")
    }
  }
  .displayName("Timer")
  .description("A an example control that runs a timer.")
  }
}

@available(iOS 18.0, *)
extension OctoconWidgetControl {
    struct Provider: ControlValueProvider {
        var previewValue: Bool {
            false
        }

        func currentValue() async throws -> Bool {
            let isRunning = true // Check if the timer is running
            return isRunning
        }
    }
}

@available(iOS 18.0, *)
struct StartTimerIntent: SetValueIntent {
    static let title: LocalizedStringResource = "Start a timer"

    @Parameter(title: "Timer is running")
    var value: Bool

    func perform() async throws -> some IntentResult {
        // Start / stop the timer based on `value`.
        return .result()
    }
}
