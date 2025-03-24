import UIKit
import SwiftUI

import shared

struct ComposeView: UIViewControllerRepresentable {
  let root: RootComponent
  let backDispatcher: Back_handlerBackDispatcher
  
  func makeUIViewController(context: Context) -> UIViewController {
    return Main_iosKt.MainViewController(
      platformDelegate: SwiftPlatformDelegate.shared,
      root: root,
      backDispatcher: backDispatcher
    )
  }

  func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
  let linkHandler = IosLinkHandler.init()
  let root: RootComponent
  let backDispatcher: Back_handlerBackDispatcher
  
  var body: some View {
    ComposeView(root: root, backDispatcher: backDispatcher)
    .ignoresSafeArea(edges: .all)
    .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    .onOpenURL { incomingURL in
      print("App was opened via URL: \(incomingURL)")
      linkHandler.onDeepLinkReceived(url: incomingURL.absoluteString)
    }
  }
}
