package app.octocon.app.utils

import platform.Foundation.NSUUID

actual fun generateUUID(): String {
  return NSUUID().UUIDString
}