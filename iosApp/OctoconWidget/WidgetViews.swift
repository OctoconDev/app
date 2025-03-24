import SwiftUI

extension Collection {
  // Returns the element at the specified index if it is within bounds, otherwise nil.
  subscript(safe index: Index) -> Element? {
    indices.contains(index) ? self[index] : nil
  }
}

extension View {
  @ViewBuilder func `if`<Content: View>(_ condition: Bool, transform: (Self) -> Content) -> some View {
    if condition {
      transform(self)
    } else {
      self
    }
  }
}

extension Color {
  init(hex: String) {
    let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
    var int: UInt64 = 0
    Scanner(string: hex).scanHexInt64(&int)
    let a, r, g, b: UInt64
    switch hex.count {
      case 3: // RGB (12-bit)
        (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
      case 6: // RGB (24-bit)
        (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
      case 8: // ARGB (32-bit)
        (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
      default:
        (a, r, g, b) = (1, 1, 1, 0)
    }
    
    self.init(
      .sRGB,
      red: Double(r) / 255,
      green: Double(g) / 255,
      blue:  Double(b) / 255,
      opacity: Double(a) / 255
    )
  }
}


struct AlterAvatar: View {
  let isSmall: Bool
  
  let data: AlterFrontDataWithImage
  
  init(data: AlterFrontDataWithImage, isSmall: Bool = false) {
    self.data = data
    self.isSmall = isSmall
  }
  
  private func firstNonSymbolLetter(from input: String?) -> String {
    // Check if the string is nil or empty
    guard let input = input, !input.isEmpty else {
      return "?"
    }
    
    // Iterate through the string to find the first non-symbol letter
    for character in input {
      if character.isLetter || character.isNumber {
        return String(character)
      }
    }
    
    // If no non-symbol letter is found, return "?"
    return "?"
  }
  
  var body: some View {
    ZStack(alignment: .bottomTrailing) {
      if let avatar = data.alter.avatar {
        if #available(iOSApplicationExtension 18.0, *) {
          Image(uiImage: avatar)
            .resizable()
            .widgetAccentedRenderingMode(.accentedDesaturated)
            .aspectRatio(contentMode: .fill)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .clipped()
            .clipShape(RoundedRectangle(cornerRadius: 12))
        } else {
          Image(uiImage: avatar)
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .clipped()
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        //          .if(data.alter.color != nil) { view in
        //            view.overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: data.alter.color!), lineWidth: 1))
        //          }
      } else {
        Text(firstNonSymbolLetter(from: data.alter.name))
          .font(.system(size: isSmall ? 18 : 26, weight: .semibold))
          .foregroundStyle(.secondary)
          .frame(maxWidth: .infinity, maxHeight: .infinity)
          .background(.quaternary)
          .clipShape(RoundedRectangle(cornerRadius: 12))
        //          .if(data.alter.color != nil) { view in
        //            view.overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(hex: data.alter.color!).opacity(0.5), lineWidth: 1))
        //          }
      }
    }
  }
}

struct OctoconSmallWidget: View {
  let data: OctoconWidgetData
  
  @ViewBuilder
  private func maybeGenerateCell(_ frontingData: [AlterFrontDataWithImage], _ index: Int) -> some View {
    if let data = frontingData[safe: index] {
      AlterAvatar(data: data)
    } else {
      // Hack for empty cell
      Color.clear
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
  }
  
  var body: some View {
    if (data.frontingData.isEmpty) {
      VStack {
        Text("No one is fronting!")
      }.padding(16)
    } else {
      let data = data.frontingData
      let overflowBadgeCount: Int? = if(data.count > 4) { data.count - 4 } else { nil }
      
      ZStack {
        GeometryReader { geometry in
          let sideLength = geometry.size.width / 2
          LazyVGrid(
            columns: Array(repeating: GridItem(.fixed(sideLength), spacing: 8), count: 2),
            spacing: 8
          ) {
            ForEach(0..<4) { index in
              maybeGenerateCell(data, index)
                .frame(width: sideLength, height: sideLength)
            }
          }
          .frame(width: geometry.size.width, height: geometry.size.width)
        }
        .aspectRatio(1, contentMode: .fit)
        
        if let overflowBadgeCount {
          Text("+\(overflowBadgeCount) more")
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(.secondary)
            .padding(5)
            .background(.thinMaterial)
            .clipShape(Capsule())
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .offset(x: 0, y: 8)
        }
      }
      .padding(16)
    }
  }
}

struct OctoconMediumWidget: View {
  let data: OctoconWidgetData
  
  @ViewBuilder
  private func maybeGenerateCell(_ frontingData: [AlterFrontDataWithImage], _ index: Int) -> some View {
    if let data = frontingData[safe: index] {
      HStack {
        AlterAvatar(data: data, isSmall: true)
          .frame(maxHeight: .infinity)
          .aspectRatio(1, contentMode: .fit)
        Text(data.alter.name ?? "Unnamed alter")
          .font(.system(size: 14))
          .lineLimit(2)
          .truncationMode(.tail)
          .foregroundStyle(.primary)
        Spacer()
      }
      .frame(maxWidth: .infinity, maxHeight: .infinity)
      .background(.quinary)
      .clipShape(RoundedRectangle(cornerRadius: 12))
    } else {
      // Hack for empty cell
      Color.clear
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
  }
  
  var body: some View {
    if (data.frontingData.isEmpty) {
      VStack {
        Text("No one is fronting!")
      }.padding(16)
    } else {
      let data = data.frontingData
      let overflowBadgeCount: Int? = if(data.count > 6) { data.count - 6 } else { nil }
      
      ZStack {
        GeometryReader { geometry in
          let columnCount = 2
          let rowCount = 3
          let totalCells = rowCount * columnCount
          let cellWidth = geometry.size.width / CGFloat(columnCount)
          let cellHeight = geometry.size.height / CGFloat(rowCount)
          
          LazyVGrid(
            columns: Array(repeating: GridItem(.fixed(cellWidth), spacing: 8), count: columnCount),
            spacing: 8
          ) {
            ForEach(0..<totalCells, id: \.self) { index in
              maybeGenerateCell(data, index)
                .frame(width: cellWidth, height: cellHeight)
            }
          }
          .frame(maxWidth: geometry.size.width, maxHeight: geometry.size.height)
        }
        
        if let overflowBadgeCount {
          Text("+\(overflowBadgeCount) more")
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(.secondary)
            .padding(5)
            .background(.thinMaterial)
            .clipShape(Capsule())
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .offset(x: 0, y: 16)
        }
      }
      .padding(EdgeInsets(top: 20, leading: 16, bottom: 20, trailing: 16))
    }
  }
}

struct OctoconLargeWidget: View {
  let data: OctoconWidgetData
  
  @ViewBuilder
  private func maybeGenerateCell(_ frontingData: [AlterFrontDataWithImage], _ index: Int) -> some View {
    if let data = frontingData[safe: index] {
      let frontingSince = try? Date.ISO8601FormatStyle().parse(data.front.time_start)
      HStack {
        AlterAvatar(data: data, isSmall: true)
          .padding(8)
          .frame(maxHeight: .infinity)
          .aspectRatio(1, contentMode: .fit)
        VStack(alignment: .leading) {
          Text(data.alter.name ?? "Unnamed alter")
            .font(.system(size: 14))
            .lineLimit(2)
            .truncationMode(.tail)
            .foregroundStyle(.primary)
          if(frontingSince != nil) {
            Text(frontingSince!, style: .relative)
              .font(.system(size: 12))
              .lineLimit(1)
              .truncationMode(.tail)
              .foregroundStyle(.secondary)
          }
        }
        .padding(EdgeInsets(top: 0, leading: -8, bottom: 0, trailing: 0))
        Spacer()
      }
      .frame(maxWidth: .infinity, maxHeight: .infinity)
      .background(.quinary)
      .clipShape(RoundedRectangle(cornerRadius: 12))
    } else {
      // Hack for empty cell
      Color.clear
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
  }
  
  var body: some View {
    if (data.frontingData.isEmpty) {
      VStack {
        Text("No one is fronting!")
      }.padding(16)
    } else {
      let data = data.frontingData
      let overflowBadgeCount: Int? = if(data.count > 10) { data.count - 10 } else { nil }
      
      ZStack {
        GeometryReader { geometry in
          let columnCount = 2
          let rowCount = 5
          let totalCells = rowCount * columnCount
          let cellWidth = geometry.size.width / CGFloat(columnCount)
          let cellHeight = geometry.size.height / CGFloat(rowCount)
          
          LazyVGrid(
            columns: Array(repeating: GridItem(.fixed(cellWidth), spacing: 8), count: columnCount),
            spacing: 8
          ) {
            ForEach(0..<totalCells, id: \.self) { index in
              maybeGenerateCell(data, index)
                .frame(width: cellWidth, height: cellHeight)
            }
          }
          .frame(maxWidth: geometry.size.width, maxHeight: geometry.size.height)
        }
        
        if let overflowBadgeCount {
          Text("+\(overflowBadgeCount) more")
            .font(.system(size: 12, weight: .semibold))
            .foregroundStyle(.secondary)
            .padding(5)
            .background(.thinMaterial)
            .clipShape(Capsule())
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            .offset(x: 0, y: 24)
        }
      }
      .padding(EdgeInsets(top: 28, leading: 16, bottom: 28, trailing: 16))
    }
  }
}
