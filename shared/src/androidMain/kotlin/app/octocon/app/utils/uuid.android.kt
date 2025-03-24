package app.octocon.app.utils

import java.util.UUID

actual fun generateUUID(): String {
  return UUID.randomUUID().toString()
}