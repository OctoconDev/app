package app.octocon.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import app.octocon.app.utils.fuse.Fuse

/*fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
  val lhsLength = lhs.length
  val rhsLength = rhs.length

  if (lhsLength == 0) return rhsLength
  if (rhsLength == 0) return lhsLength

  val matrix = Array(lhsLength + 1) { IntArray(rhsLength + 1) }

  for (i in 0..lhsLength) {
    matrix[i][0] = i
  }
  for (j in 0..rhsLength) {
    matrix[0][j] = j
  }

  for (i in 1..lhsLength) {
    for (j in 1..rhsLength) {
      val cost = if (lhs[i - 1] == rhs[j - 1]) 0 else 1
      matrix[i][j] = minOf(
        matrix[i - 1][j] + 1,
        matrix[i][j - 1] + 1,
        matrix[i - 1][j - 1] + cost
      )
    }
  }

  return matrix[lhsLength][rhsLength]
}

fun minLevenshteinDistance(substring: String, fullString: String): Int {
  val substringLength = substring.length
  val fullStringLength = fullString.length

  if (substringLength == 0) return 0
  if (fullStringLength == 0) return substringLength

  var minDistance = Int.MAX_VALUE

  for (i in 0..(fullStringLength - substringLength)) {
    val distance = levenshteinDistance(substring, fullString.substring(i, i + substringLength))
    if (distance < minDistance) {
      minDistance = distance
    }
  }

  return minDistance
}

suspend fun <T> List<T>.sortBySimilarity(
  accessor: (T) -> String,
  searchString: String,
  maxDistance: Int
): List<T> = coroutineScope {
  val lowercaseSearchString = searchString.lowercase()
  val resultList = mutableListOf<Pair<T, Int>>()
  val mutex = Mutex()

  this@sortBySimilarity.map {
    launch {
      val distance = minLevenshteinDistance(lowercaseSearchString, accessor(it).lowercase())
      if (distance <= maxDistance) {
        mutex.withLock {
          resultList.add(it to distance)
        }
      }
    }
  }.joinAll()

  resultList.sortBy { it.second }
  return@coroutineScope resultList.map { it.first }
}*/

/**
 * Sorts a list of items by similarity to a search string using a fuzzy search algorithm adapted
 * from Fuse.js.
 *
 * @param accessor A function that extracts a string from an item.
 * @param searchString The search string.
 * @param threshold The maximum similarity score for an item to be included in the result. 0.0 means
 * a perfect match, 1.0 means no match. The default value is 0.6.
 * @param fuse The Fuse object to use for the fuzzy search. The default value is a new Fuse object
 * with the specified threshold.
 *
 */
suspend fun <T> List<T>.sortBySimilarity(
  accessor: (T) -> String,
  searchString: String,
  threshold: Double = 0.6,
  fuse: Fuse = Fuse(threshold = threshold),
): List<T> = coroutineScope {
  withContext(Dispatchers.Default) {
    val pattern = fuse.createPattern(searchString)

    val resultList = ArrayList<Pair<T, Double>>(this@sortBySimilarity.size)
    val mutex = Mutex()

    this@sortBySimilarity.map { item ->
      launch {
        val score = fuse.search(pattern, accessor(item))?.first ?: 1.0
        if (score <= threshold) {
          // Use mutex to ensure thread-safe operations on resultList
          mutex.withLock {
            resultList.add(item to score)
          }
        }
      }
    }.joinAll() // Wait for all coroutines to complete
    resultList.sortBy { it.second }
    resultList.map { it.first }
  }
  /*
    // Second implementation: lazy evaluation (functional)

  val distances =
      this@sortBySimilarity.asSequence().map { fuse.search(pattern, accessor(it))?.first ?: 1.0 }

    return@coroutineScope this@sortBySimilarity.asSequence().zip(distances)
      .filter { it.second <= threshold }
      .sortedBy { it.second }
      .map { it.first }
      .toImmutableList()


    // Third implementation: eager evaluation (functional)

  val distances = this.map { fuse.search(pattern, accessor(it))?.first ?: 1.0 }

    return this.zip(distances)
      .filter { it.second <= threshold }
      .sortedBy { it.second }
      .map { it.first }*/
}
