@file:Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth", "ReturnCount")

package app.octocon.app.utils.fuse

import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Lightweight fuzzy search library. Translated from the original fuse-swift
 * @see [https://github.com/krisk/fuse-swift]
 *
 * Creates a new instance of Fuse.
 *
 * @param location Approximately where in the text is the pattern expected to be found. Defaults to `0`.
 * @param distance Determines how close the match must be to the fuzzy `location` (specified above).
 * An exact letter match which is `distance` characters away from the fuzzy location would score as a complete mismatch.
 * A distance of `0` requires the match be at the exact `location` specified,
 * a `distance` of `1000` would require a perfect match to be within `800` characters of the fuzzy
 * location to be found using a 0.8 threshold. Defaults to `100`.
 * @param threshold At what point does the match algorithm give up.
 * A threshold of `0.0` requires a perfect match (of both letters and location),
 * a threshold of `1.0` would match anything. Defaults to `0.6`.
 * @param isCaseSensitive Indicates whether comparisons should be case sensitive. Defaults to `false`.
 * @param tokenize When true, the search algorithm will search individual words **and** the full string,
 * computing the final score as a function of both. Note that when `tokenize` is `true`,
 * the `threshold`, `distance`, and `location` are inconsequential for individual tokens.
 */
class Fuse(
  private val location: Int = 0,
  private val distance: Int = 100,
  private val threshold: Double = 0.6,
  private val isCaseSensitive: Boolean = false,
  private val tokenize: Boolean = false
) {

  data class Pattern(
    val text: String,
    val len: Int,
    val mask: Int,
    val alphabet: Map<Char, Int>
  )

  data class SearchResult(
    val index: Int,
    val score: Double,
    val ranges: List<ClosedRange<Int>>
  )

  /**
   * Creates a pattern tuple.
   *
   * @param str A string from which to create the pattern tuple.
   *
   * @return a tuple containing pattern metadata.
   */
  fun createPattern(str: String): Pattern? {
    val pattern = if (isCaseSensitive) str else str.lowercase()
    val len = pattern.length
    if (len == 0) {
      return null
    }

    return Pattern(
      pattern,
      len,
      1 shl (len - 1),
      FuseUtilities.calculatePatternAlphabet(pattern)
    )
  }

  /**
   * Searches for a pattern in a given string.
   *
   * @param pattern The pattern to search for. This is created by calling `createPattern`.
   * @param str The string in which to search for the pattern.
   *
   * @return a tuple containing a `score` between `0.0` (exact match) and `1` (not a match),
   * and `ranges` of the matched characters. If no match is found will return null.
   */
  fun search(pattern: Pattern?, str: String): Pair<Double, List<IntRange>>? {
    if (pattern == null)
      return null

    // If tokenize is set we will split the pattern into individual words and take the average
    // which should result in more accurate matches
    if (tokenize) {
      // Split this pattern by the space character
      val wordPatterns = pattern.text.split(" ").mapNotNull { createPattern(it) }
      // Get the result for testing the full pattern string.
      // If 2 strings have equal individual word matches this will boost the full string
      // that matches best overall to the top.
      val fullPatternResult = doSearch(pattern, str)
      // Reduce all the word pattern matches and the full pattern match into a totals tuple.
      val results = wordPatterns.fold(fullPatternResult) { partialResult, pat ->
        val result = doSearch(pat, str)
        (partialResult.first + result.first) to (partialResult.second + result.second)
      }

      // Average the total score by dividing the summed scores by the number of word
      // searches + the full string search. Also remove any range duplicates since we are
      // searching full string and words individually.
      val averagedResult =
        results.first / (wordPatterns.size + 1).toDouble() to results.second.distinct()
      // If the averaged score is 1 then there were no matches so return nil.
      // Otherwise return the average result
      return if (averagedResult.first == 1.0) null else averagedResult
    } else {
      val result = doSearch(pattern, str)
      // If the averaged score is 1 then there were no matches so return nil.
      // Otherwise return the average result
      return if (result.first == 1.0) null else result
    }
  }

  private fun doSearch(pattern: Pattern, str: String): Pair<Double, List<IntRange>> {
    var text = str
    val textLength = text.length

    if (!isCaseSensitive) {
      text = text.lowercase()
    }

    // Exact match
    if (pattern.text == text) {
      return 0.0 to listOf(0 until textLength)
    }

    val location = location
    val distance = distance
    var threshold = threshold
    val index = text.indexOf(pattern.text, location)
    var bestLocation = if (index > 0) index else null

    // A mask of the matches. We'll use to determine all the ranges of the matches
    val matchMaskArr = MutableList(textLength) { 0 }
    val bestLoc = bestLocation
    if (bestLoc != null) {
      threshold = min(
        threshold,
        FuseUtilities.calculateScore(
          pattern.text,
          e = 0,
          x = location,
          loc = bestLoc,
          distance = distance
        )
      )

      // What about in the other direction? (speed up)
      bestLocation = run {
        val index1 = text.lastIndexOf(pattern.text, location + pattern.len)
        if (index1 > 0) index1 else null
      }

      if (bestLocation != null) {
        threshold = min(
          threshold,
          FuseUtilities.calculateScore(
            pattern.text,
            e = 0,
            x = location,
            loc = bestLocation,
            distance = distance
          )
        )
      }
    }

    var score = 1.0
    var binMax = pattern.len + textLength
    var lastBitArr = listOf<Int>()

    // Magic begins now
    for (i in 0 until pattern.len) {
      // Scan for the best match; each iteration allows for one more error.
      // Run a binary search to determine how far from the match location we can stray at this error level.
      var binMin = 0
      var binMid = binMax

      while (binMin < binMid) {
        if (FuseUtilities.calculateScore(
            pattern.text,
            e = i,
            x = location,
            loc = location + binMid,
            distance = distance
          ) <= threshold
        ) {
          binMin = binMid
        } else {
          binMax = binMid
        }
        binMid = floor((binMax - binMin).toDouble() / 2 + binMin.toDouble()).toInt()
      }
      // Use the result from this iteration as the maximum for the next.
      binMax = binMid
      val start = max(1, location - binMid + 1)
      val finish = min(location + binMid, textLength) + pattern.len

      // Initialize the bit array
      val bitArr = MutableList(finish + 2) { 0 }
      bitArr[finish + 1] = (1 shl i) - 1

      if (start > finish) {
        continue
      }

      for (j in (start..finish).reversed()) {
        val currentLocation = j - 1

        // Need to check for `nil` case, since `patternAlphabet` is a sparse hash
        val charMatch = try {
          pattern.alphabet[text[currentLocation]] ?: 0
        } catch (e: IndexOutOfBoundsException) {
          0
        }

        // A match is found
        if (charMatch != 0) {
          matchMaskArr[currentLocation] = 1
        }

        // First pass: exact match
        bitArr[j] = ((bitArr[j + 1] shl 1) or 1) and charMatch

        // Subsequent passes: fuzzy match
        if (i > 0) {
          bitArr[j] = bitArr[j] or
              (((lastBitArr[j + 1] or lastBitArr[j]) shl 1) or 1) or lastBitArr[j + 1]
        }

        if ((bitArr[j] and pattern.mask) != 0) {
          score = FuseUtilities.calculateScore(
            pattern.text,
            e = i,
            x = location,
            loc = currentLocation,
            distance = distance
          )

          // This match will almost certainly be better than any existing match, but check anyway.
          if (score <= threshold) {
            // Indeed it is
            threshold = score
            bestLocation = currentLocation

            if (bestLocation <= location || bestLocation < 0) {
              // Already passed `location`. No point in continuing.
              break
            }
          }
        }
      }

      // No hope for a better match at greater error levels
      if (FuseUtilities.calculateScore(
          pattern.text,
          e = i + 1,
          x = location,
          loc = location,
          distance = distance
        ) > threshold
      ) {
        break
      }

      lastBitArr = bitArr
    }

    return score to FuseUtilities.findRanges(matchMaskArr.toIntArray())
  }
}