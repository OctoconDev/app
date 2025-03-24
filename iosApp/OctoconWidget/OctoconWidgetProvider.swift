import WidgetKit
import SDWebImage
import KeychainSwift
import Alamofire

struct OctoconWidgetProvider: TimelineProvider {
  private let jsonDecoder: JSONDecoder
  private let jsonEncoder: JSONEncoder
  private let groupDefaults: UserDefaults
  private let imageManager: SDWebImageManager
  
  init() {
    let decoder = JSONDecoder()
    decoder.keyDecodingStrategy = .convertFromSnakeCase
    self.jsonDecoder = decoder
    
    self.jsonEncoder = JSONEncoder()
    
    self.groupDefaults = UserDefaults(suiteName: "group.app.octocon.Octocon")!
    self.imageManager = SDWebImageManager.shared
  }
  
  func placeholder(in context: Context) -> OctoconWidgetEntry {
    OctoconWidgetEntry(date: .now, state: .data(OctoconWidgetData(frontingData: [])))
  }
  
  func getSnapshot(in context: Context, completion: @escaping (OctoconWidgetEntry) -> ()) {
    completion(generateDummyEntry())
  }
  
  func getTimeline(in context: Context, completion: @escaping (Timeline<Entry>) -> ()) {
    
    func generateErrorTimeline(error: OctoconWidgetError) -> Timeline<Entry> {
      Timeline(entries: [
        OctoconWidgetEntry(date: .now, state: .error(error))
      ] as [OctoconWidgetEntry], policy: .atEnd)
    }
    let keychain = KeychainSwift()
    keychain.accessGroup = "AVJM9TZ9VF.app.octocon.OctoconApp.Keychain"
    
    if(groupDefaults.bool(forKey: "invalidate")) {
      groupDefaults.removeObject(forKey: "cachedFrontWidgetEntry")
      groupDefaults.set(false, forKey: "invalidate")
    }

    guard let settingsJson = keychain.get("settings") else {
      Task {
        let cachedEntry = await tryGetCachedEntry()
        completion(Timeline(entries: [cachedEntry], policy: .atEnd))
      }
      return
    }
    
    guard let settings = try? jsonDecoder.decode(PartialAppSettings.self, from: settingsJson.data(using: .utf8)!) else {
      completion(generateErrorTimeline(error: .NOT_LOGGED_IN))
      return
    }
    
    if(settings.token == nil) {
      completion(generateErrorTimeline(error: .NOT_LOGGED_IN))
      return
    }
    
    if(settings.tokenIsProtected == true) {
      completion(generateErrorTimeline(error: .PIN_IS_PROTECTED))
      return
    }
    
    Task {
      let headers: HTTPHeaders = [
        "Authorization": "Bearer \(settings.token!)",
        "User-Agent": "Octocon iOS v\(Bundle.main.releaseVersionNumber ?? "UNKNOWN") (\(Bundle.main.buildVersionNumber ?? "UNKNOWN"))"
      ]
      
      let response = await AF.request("https://api.octocon.app/api/systems/me/fronting", headers: headers).serializingDecodable(FrontingResponse.self).response
      
      switch response.result {
        case .success(let frontingResponse):
          if let encoded = try? jsonEncoder.encode(frontingResponse.data) {
            groupDefaults.set(encoded, forKey: "cachedFrontWidgetEntry")
          }
          let dataWithImages = await loadImagesAsyncConcurrently(from: frontingResponse.data)
          
          let timeline = Timeline(entries: [
            OctoconWidgetEntry(date: .now, state: .data(OctoconWidgetData(frontingData: dataWithImages)))
          ], policy: .atEnd)
          completion(timeline)
        case .failure(let error):
          NSLog(error.localizedDescription)
          
          completion(generateErrorTimeline(error: .NETWORK_ERROR))
          return
      }
    }
    
    func tryGetCachedEntry() async -> Entry {
      guard let cachedRawData = groupDefaults.data(forKey: "cachedFrontWidgetEntry") else {
        return OctoconWidgetEntry(date: .now, state: .error(.NOT_LOGGED_IN))
      }
      
      guard let cachedData = try? jsonDecoder.decode([AlterFrontData].self, from: cachedRawData) else {
        return OctoconWidgetEntry(date: .now, state: .error(.NOT_LOGGED_IN))
      }
      
      let dataWithImages = await loadImagesAsyncConcurrently(from: cachedData)
      
      return OctoconWidgetEntry(date: .now, state: .data(OctoconWidgetData(frontingData: dataWithImages)))
    }
    
    @MainActor
    func loadImagesAsyncConcurrently(from data: [AlterFrontData]) async -> [AlterFrontDataWithImage] {
      // Create a list of tasks for concurrent image loading
      async let concurrentImages: [Int: AlterFrontDataWithImage] = await withTaskGroup(of: (Int, AlterFrontDataWithImage).self) { group in
        for (index, frontData) in data.enumerated() {
          group.addTask {
            if frontData.alter.avatar_url == nil {
              let result = AlterFrontDataWithImage(primary: frontData.primary, alter: AlterWithImage(
                id: frontData.alter.id,
                name: frontData.alter.name,
                pronouns: frontData.alter.pronouns,
                color: frontData.alter.color,
                avatar: nil
              ), front: frontData.front)
              return (index, result)
            }
            
            guard let url = URL(string: frontData.alter.avatar_url!) else {
              let result = AlterFrontDataWithImage(primary: frontData.primary, alter: AlterWithImage(
                id: frontData.alter.id,
                name: frontData.alter.name,
                pronouns: frontData.alter.pronouns,
                color: frontData.alter.color,
                avatar: nil
              ), front: frontData.front)
              return (index, result)
            }
            
            do {
              let image = try await loadImageAsync(from: url) // Assuming `loadImageAsync` is async
              let result = AlterFrontDataWithImage(primary: frontData.primary, alter: AlterWithImage(
                id: frontData.alter.id,
                name: frontData.alter.name,
                pronouns: frontData.alter.pronouns,
                color: frontData.alter.color,
                avatar: image
              ), front: frontData.front)
              return (index, result)
            } catch {
              NSLog("Failed to load image: \(error)")
              let result = AlterFrontDataWithImage(primary: frontData.primary, alter: AlterWithImage(
                id: frontData.alter.id,
                name: frontData.alter.name,
                pronouns: frontData.alter.pronouns,
                color: frontData.alter.color,
                avatar: nil
              ), front: frontData.front)
              return (index, result)
            }
          }
        }
        
        // Wait for all tasks to complete and collect the results
        var resultDict: [Int: AlterFrontDataWithImage] = [:]
        for await (index, result) in group {
          resultDict[index] = result
        }
        
        return resultDict
      }
      
      return await concurrentImages.sorted { $0.key < $1.key }.map { $0.value }
    }
    
    
    // Async wrapper for loadImage
    @Sendable
    func loadImageAsync(from url: URL) async throws -> UIImage {
      return try await withCheckedThrowingContinuation { continuation in
        imageManager.loadImage(with: url, context: [.imageForceDecodePolicy: SDImageForceDecodePolicy.never.rawValue], progress: nil) { image, _, _, _, _, _ in
          if let image = image {
            continuation.resume(returning: image)
          } else {
            continuation.resume(throwing: NSError(domain: "ImageLoadError", code: -1, userInfo: nil))
          }
        }
      }
    }
    
  }
  
  //    func relevances() async -> WidgetRelevances<Void> {
  //        // Generate a list containing the contexts this widget is relevant in.
  //    }
}
