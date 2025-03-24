package app.octocon.app.utils.fuse

/**
 * The string in which to search for the pattern.
 *
 * <pre>
 *     val fuse = Fuse()
 *     fuse.search("some text", listOf("some string"))
 * </pre>
 *
 * <b>Note</b>: if the same text needs to be searched across many strings,
 * consider creating the pattern once via `createPattern`,
 * and then use the other `search` function.
 * This will improve performance, as the pattern object would only be created once,
 * and re-used across every search call:
 *
 * <pre>
 *     val fuse = Fuse()
 *     val pattern = fuse.createPattern("some text")
 *     fuse.search(pattern, "some string")
 *     fuse.search(pattern, "another string")
 *     fuse.search(pattern, "yet another string")
 * </pre>
 *
 * @param text the text string to search for.
 * @param str the string in which to search for the pattern.
 *
 * @return a tuple containing a `score` between `0.0` (exact match) and `1` (not a match), and
 * `ranges` of the matched characters.
 */
fun Fuse.search(text: String, str: String): Pair<Double, List<IntRange>>? =
  search(createPattern(text), str)

/**
 * Searches for a text pattern in an array of strings.
 *
 * @param text the pattern string to search for.
 * @param strings the list of strings in which to search.
 *
 * @return a tuple containing the `item` in which the match is found, the `score`, and the `ranges`
 * of the matched characters. Sorted by score.
 */
fun Fuse.search(text: String, strings: List<String>): List<Fuse.SearchResult> {
  val pattern = this.createPattern(text)
  val items = mutableListOf<Fuse.SearchResult>()
  strings.forEachIndexed { index, item ->
    val result = search(pattern, item)
    if (result != null) {
      items.add(Fuse.SearchResult(index, result.first, result.second))
    }
  }

  return items.sortedBy { it.score }
}