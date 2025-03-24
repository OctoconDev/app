import Foundation
import CryptoKit
import JOSESwift
import WidgetKit

import class shared.Platform_iosKt
import protocol shared.PlatformDelegate

class Throttler {
  private var workItem: DispatchWorkItem?
  private let block: (() -> Void)
  private let queue: DispatchQueue
  private let delay: TimeInterval
  
  init(delay: TimeInterval, queue: DispatchQueue = .main, block: @escaping () -> Void) {
    self.delay = delay
    self.queue = queue
    self.block = block
  }
  
  func throttle() {
    workItem?.cancel()
    
    workItem = DispatchWorkItem { [weak self] in
      guard let self = self else { return }
      self.block()
      self.workItem = nil
    }

    if let workItem = workItem {
      queue.asyncAfter(deadline: .now() + delay, execute: workItem)
    }
  }
  
  func consumeNow() {
    guard let workItem = workItem else {
      return
    }
    
    workItem.cancel()
    block()

    self.workItem = nil
  }
}


class SwiftPlatformDelegate : PlatformDelegate {
  static let shared = SwiftPlatformDelegate()
  private let groupDefaults = UserDefaults(suiteName: "group.app.octocon.Octocon")!
  
  let widgetThrottler = Throttler(delay: 5.0) {
    WidgetCenter.shared.reloadAllTimelines()
  }
  
  func decryptData(key: Data, iv: Data, cipherText: Data, tag: Data) -> Optional<String> {
    do {
      let box = try AES.GCM.SealedBox(nonce: AES.GCM.Nonce(data: iv), ciphertext: cipherText, tag: tag)
      let data = try AES.GCM.open(box, using: SymmetricKey(data: key))

      return String(data: data, encoding: .utf8)!
    } catch {
      return nil
    }
  }
  
  func encryptData(key: Data, iv: Data, plainText: String) -> Data {
    let box = try! AES.GCM.seal(plainText.data(using: .utf8)!, using: SymmetricKey(data: key), nonce: AES.GCM.Nonce(data: iv))
    
    let cipherText = box.ciphertext
    let tag = box.tag
    
    let combinedData = NSMutableData()
    
    combinedData.append(cipherText)
    combinedData.append(tag)
    return combinedData as Data
  }
  
  func recoveryCodeToJWE(recoveryCode: String) -> String {
    let header = JWEHeader(keyManagementAlgorithm: .RSAOAEP256, contentEncryptionAlgorithm: .A256GCM)
    let payload = Payload(recoveryCode.data(using: .utf8)!)
    
    let publicKeyString = Platform_iosKt.getOctoconPublicKey()
    let publicKey = publicKeyFromString(publicKeyString)
    
    let encrypter = Encrypter(keyManagementAlgorithm: .RSAOAEP256, contentEncryptionAlgorithm: .A256GCM, encryptionKey: publicKey)!
    let jwe = try! JWE(header: header, payload: payload, encrypter: encrypter)
    
    return jwe.compactSerializedString
  }
  
  func updateWidgets(sessionInvalidated: Bool) {
    if(sessionInvalidated) {
      Task {
        groupDefaults.set(true, forKey: "invalidate")
        WidgetCenter.shared.reloadAllTimelines()
      }
    } else {
      widgetThrottler.throttle()
    }
  }
  
  private func publicKeyFromString(_ keyString: String) -> SecKey {
    let publicKeyStringWithoutHeaders = keyString.replacingOccurrences(of: "-----BEGIN PUBLIC KEY-----", with: "").replacingOccurrences(of: "-----END PUBLIC KEY-----", with: "")
    
    let publicKeyData = NSData(base64Encoded: publicKeyStringWithoutHeaders, options: NSData.Base64DecodingOptions.ignoreUnknownCharacters)!
    
    let keyDict: [NSString: AnyObject] = [
      kSecAttrKeyType: kSecAttrKeyTypeRSA,
      kSecAttrKeyClass: kSecAttrKeyClassPublic,
      kSecAttrKeySizeInBits: 2048 as AnyObject
    ]
    var error: Unmanaged<CFError>?
    
    return SecKeyCreateWithData(publicKeyData as CFData, keyDict as CFDictionary, &error)!
  }
}
