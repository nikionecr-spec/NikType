package com.niktype.prediction.dictionary

/**
 * درخت پیشوندی (Trie) برای جستجوی سریع واژه‌ها:
 * جستجوی دقیق، جستجوی پیشوندی و جستجوی فازی (فاصله لونشتاین محدود).
 *
 * پیچیدگی جستجوی دقیق/پیشوندی: O(len)
 * پیچیدگی فازی: O(n * len * maxDist) با هرس زودهنگام
 */
class Trie {

    private class Node {
        val children = HashMap<Char, Node>()
        var freq = 0
    }

    private val root = Node()

    var size = 0
        private set

    fun insert(word: String, freq: Int) {
        var node = root
        for (c in word) {
            node = node.children.getOrPut(c) { Node() }
        }
        if (node.freq == 0) size++
        if (freq > node.freq) node.freq = freq
    }

    fun lookup(word: String): WordEntry? {
        var node = root
        for (c in word) {
            node = node.children[c] ?: return null
        }
        if (node.freq == 0) return null
        return WordEntry(word, node.freq)
    }

    fun prefixWords(prefix: String, limit: Int = 20): List<WordEntry> {
        var node = root
        for (c in prefix) {
            node = node.children[c] ?: return emptyList()
        }
        val collected = ArrayList<WordEntry>()
        dfs(node, StringBuilder(prefix), collected)
        collected.sortByDescending { it.freq }
        return collected.take(limit)
    }

    private fun dfs(node: Node, sb: StringBuilder, out: MutableList<WordEntry>) {
        if (out.size >= 400) return
        if (node.freq > 0) out.add(WordEntry(sb.toString(), node.freq))
        for ((c, child) in node.children) {
            sb.append(c)
            dfs(child, sb, out)
            sb.deleteCharAt(sb.length - 1)
        }
    }

    /**
     * جستجوی فازی با الگوریتم استاندارد «لونشتاین روی درخت»:
     * برای هر گره یک ردیف ماتریس فاصله نگه داشته می‌شود و شاخه‌هایی که
     * حداقل فاصله‌شان از maxDist بیشتر است هرس می‌شوند.
     */
    fun fuzzy(word: String, maxDist: Int, limit: Int = 8): List<FuzzyMatch> {
        if (word.isEmpty()) return emptyList()
        val results = ArrayList<FuzzyMatch>()
        val row = IntArray(word.length + 1) { it }
        for ((c, child) in root.children) {
            fuzzyDfs(child, c.toString(), c, word, row, maxDist, results)
        }
        results.sortWith(compareBy({ it.distance }, { -it.entry.freq }))
        return results.take(limit)
    }

    private fun fuzzyDfs(
        node: Node,
        currentWord: String,
        ch: Char,
        word: String,
        prevRow: IntArray,
        maxDist: Int,
        results: MutableList<FuzzyMatch>,
    ) {
        val row = IntArray(word.length + 1)
        row[0] = prevRow[0] + 1
        for (col in 1..word.length) {
            val insertCost = row[col - 1] + 1
            val deleteCost = prevRow[col] + 1
            val subCost = if (word[col - 1] == ch) prevRow[col - 1] else prevRow[col - 1] + 1
            row[col] = minOf(insertCost, deleteCost, subCost)
        }
        var minRow = Int.MAX_VALUE
        for (v in row) if (v < minRow) minRow = v
        if (minRow > maxDist) return

        if (node.freq > 0 && row[word.length] <= maxDist) {
            results.add(FuzzyMatch(WordEntry(currentWord, node.freq), row[word.length]))
        }
        for ((c, child) in node.children) {
            fuzzyDfs(child, currentWord + c, c, word, row, maxDist, results)
        }
    }
}

data class WordEntry(
    val word: String,
    val freq: Int,
    val user: Boolean = false,
)

data class FuzzyMatch(
    val entry: WordEntry,
    val distance: Int,
)
