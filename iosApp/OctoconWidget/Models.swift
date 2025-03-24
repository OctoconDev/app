import UIKit
import WidgetKit

struct Alter: Codable {
  let id: UInt16
  let name: String?
  let pronouns: String?
  let color: String?
  let avatar_url: String?
}

struct AlterWithImage {
  let id: UInt16
  let name: String?
  let pronouns: String?
  let color: String?
  let avatar: UIImage?
}


struct Front: Codable {
  let time_start: String
  let time_end: String?
}

struct AlterFrontData: Codable {
  let primary: Bool
  let alter: Alter
  let front: Front
}

struct AlterFrontDataWithImage {
  let primary: Bool
  let alter: AlterWithImage
  let front: Front
}

struct FrontingResponse: Codable {
  let data: [AlterFrontData]
}

enum OctoconWidgetError {
  case PIN_IS_PROTECTED
  case NOT_LOGGED_IN
  case NETWORK_ERROR
}

struct OctoconWidgetData {
  let frontingData: [AlterFrontDataWithImage]
}

struct OctoconWidgetEntry: TimelineEntry {
  let date: Date
  let state: State

  enum State {
    case error(OctoconWidgetError)
    case data(OctoconWidgetData)
  }
}

func generateDummyEntry() -> OctoconWidgetEntry {
  return OctoconWidgetEntry(date: .now, state: .data(OctoconWidgetData(
    frontingData: [
      AlterFrontDataWithImage(
        primary: true,
        alter: AlterWithImage(id: 1, name: "Atlas", pronouns: "he/him", color: nil, avatar: nil),
        front: Front(time_start: Calendar.current.date(byAdding: .minute, value: -5, to: .now)!.formatted(.iso8601), time_end: nil)
      ),
      AlterFrontDataWithImage(
        primary: false,
        alter: AlterWithImage(id: 2, name: "Gaia", pronouns: "she/her", color: nil, avatar: nil),
        front: Front(time_start: Calendar.current.date(byAdding: .hour, value: -5, to: .now)!.formatted(.iso8601), time_end: nil)
      ),
      AlterFrontDataWithImage(
        primary: false,
        alter: AlterWithImage(id: 3, name: "Hyperion", pronouns: "they/them", color: nil, avatar: nil),
        front: Front(time_start: Calendar.current.date(byAdding: .day, value: -5, to: .now)!.formatted(.iso8601), time_end: nil))
    ]))
  )
}
