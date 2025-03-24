package app.octocon.app.utils.fuse

import kotlin.math.abs

object FuseUtilities {

  /**
   * Computes the score for a match with `e` errors and `x` location.
   *
   * @param pattern Pattern being sought.
   * @param e Number of errors in match.
   * @param x Location of match.
   * @param loc Expected location of match.
   * @param distance Distance from the correct string.
   *
   * @return overall score for a match (0.0 = good, 1.0 = bad).
   */
  fun calculateScore(pattern: String, e: Int, x: Int, loc: Int, distance: Int): Double {
    val accuracy = e.toDouble() / pattern.length.toDouble()
    val proximity = abs(x - loc)

    return if (distance == 0) {
      if (proximity != 0) {
        1.0
      } else {
        accuracy
      }
    } else {
      accuracy + (proximity.toDouble() / distance.toDouble())
    }
  }

  /**
   * Initializes the alphabet for the Bitap algorithm.
   *
   * @param pattern the text to encode.
   * @return hash of character locations.
   */
  fun calculatePatternAlphabet(pattern: String): Map<Char, Int> {
    val mask = mutableMapOf<Char, Int>()

    for (char in pattern) {
      mask[char] = 0
    }

    for (i in pattern.indices) {
      val c = pattern[i]
      mask[c] = (mask[c] ?: 0) or (1 shl (pattern.length - i - 1))
    }

    return mask
  }

  /**
   * Returns an array of `IntRanges`, where each range represents a consecutive list of `1`s.
   *
   * <pre>
   *     val arr = arrayOf(0, 1, 1, 0, 1, 1, 1)
   *     val ranges = findRanges(arr)
   *     // [{startIndex 1, endIndex 2}, {startIndex 4, endIndex 6}]
   * </pre>
   *
   * @param mask a string representing the value to search for.
   *
   * @return a list of `IntRanges`.
   */
  fun findRanges(mask: IntArray): List<IntRange> {
    val ranges = mutableListOf<IntRange>()
    var start = -1
    var end: Int

    mask.forEachIndexed { i, bit ->
      if (bit == 1 && start == -1) {
        start = i
      } else if (bit == 0 && start != -1) {
        end = i - 1
        ranges.add(start..end)
        start = -1
      }
    }

    if (mask.last() == 1) {
      ranges.add(start until mask.size)
    }

    return ranges
  }
}