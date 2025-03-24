import WidgetKit
import SwiftUI

@main
struct OctoconWidgetBundle: WidgetBundle {
    var body: some Widget {
      OctoconFrontingWidget()
        /*if #available(iOS 18.0, *) {
            OctoconWidgetControl()
        }*/
    }
}

extension Bundle {
  var releaseVersionNumber: String? {
    return infoDictionary?["CFBundleShortVersionString"] as? String
  }
  var buildVersionNumber: String? {
    return infoDictionary?["CFBundleVersion"] as? String
  }
}
