import SwiftUI
import WidgetKit

import FirebaseMessaging
import FirebaseCore
import Sentry

import shared

let delegate = SwiftPlatformDelegate()

class AppDelegate: NSObject, UIApplicationDelegate {
  private var stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: nil)
  var backDispatcher: Back_handlerBackDispatcher = DecomposeUtilsKt.createBackDispatcher()

  lazy var root: RootComponent = DecomposeUtilsKt.createRootComponent(
      componentContext: DefaultComponentContext(
        lifecycle: ApplicationLifecycle(),
        stateKeeper: stateKeeper,
        instanceKeeper: nil,
        backHandler: backDispatcher
        )
      )
  
  func application(_ application: UIApplication, shouldSaveSecureApplicationState coder: NSCoder) -> Bool {
    DecomposeUtilsKt.save(coder: coder, state: stateKeeper.save())
    return true
  }
      
  func application(_ application: UIApplication, shouldRestoreSecureApplicationState coder: NSCoder) -> Bool {
    // stateKeeper = StateKeeperDispatcherKt.StateKeeperDispatcher(savedState: StateKeeperUtilsKt.restore(coder: coder))
    return true
  }
  
  func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    SentrySDK.start { options in
      options.dsn = "https://2d3e642ffffb0f518635c9ec53be890d@o4506836867022848.ingest.us.sentry.io/4508575166169088"
      
      options.tracesSampleRate = 1.0
      options.profilesSampleRate = 1.0
    }
    
    let defaults = UserDefaults.standard
    
    if !defaults.bool(forKey: "hasPreviouslyLaunched") {
      Settings_iosKt.clearKeychain()
      defaults.set(true, forKey: "hasPreviouslyLaunched")
    }
    
    FirebaseApp.configure()
    Messaging.messaging().delegate = self
    
    Task { WidgetCenter.shared.reloadAllTimelines() }
    
    return true
  }
  
//  func applicationDidEnterBackground(_ application: UIApplication) {
//    NSLog("Entered background")
//    
//    SwiftPlatformDelegate.shared.widgetThrottler.consumeNow()
//  }
//  
//  func applicationWillResignActive(_ application: UIApplication) {
//    NSLog("Resigning")
//    
//    SwiftPlatformDelegate.shared.widgetThrottler.consumeNow()
//  }
  
  func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    NSLog("didRegisterForRemoteNotificationsWithDeviceToken")
    Messaging.messaging().apnsToken = deviceToken
  }
}

extension AppDelegate: MessagingDelegate {
  func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
    Main_iosKt.providePushNotificationToken(token: fcmToken)
  }
}

@main
struct iOSApp: App {
  @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
  
  var body: some Scene {
    WindowGroup {
      ContentView(root: delegate.root, backDispatcher: delegate.backDispatcher)
    }
  }
}
