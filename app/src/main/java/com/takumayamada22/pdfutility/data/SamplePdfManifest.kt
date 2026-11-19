package com.takumayamada22.pdfutility.data

import org.json.JSONObject
import java.net.URI
import java.util.Locale

/** Only metadata is refreshed automatically. PDF bodies remain explicit user downloads. */
internal object SamplePdfManifest {
    const val LATEST_URL = "https://raw.githubusercontent.com/tyamada/manga-prompt-kit/main/releases/pdf-manifest.json"
    const val MAX_BYTES = 2 * 1024 * 1024

    fun parse(text: String): List<SamplePdfItem> {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES)
        StrictManifestJson(text).validate()
        val tokener = org.json.JSONTokener(text)
        val root = JSONObject(tokener)
        require(tokener.nextClean() == '\u0000')
        require(root.get("schema_version") == 1)
        require(root.getString("repository") == "tyamada/manga-prompt-kit")
        require(root.getString("license") == "CC-BY-4.0")
        require(root.getString("status") == "published")
        val entries = root.getJSONArray("entries")
        require(entries.length() in SamplePdfCatalog.items.size..1000)
        require(positiveLong(root, "pdf_count") == entries.length().toLong())
        val bundled = SamplePdfCatalog.items.associateBy { it.id }
        val knownLanguages = SamplePdfCatalog.items.associateBy { it.languageCode }
        val ids = mutableSetOf<String>()
        val urls = mutableSetOf<String>()
        val result = (0 until entries.length()).map { index ->
            val entry = entries.getJSONObject(index)
            val id = entry.getString("sample_id")
            require(id.matches(Regex("ep[1-9][0-9]{0,3}_[a-z0-9_]+")) && ids.add(id))
            val language = entry.getString("language_key")
            val languageTemplate = requireNotNull(knownLanguages[language])
            val episode = positiveLong(entry, "episode")
            require(id == "ep${episode}_$language")
            val locale = entry.getString("locale")
            require(locale == languageTemplate.locale)
            val filename = entry.getString("filename")
            require(filename.matches(Regex("[A-Za-z0-9_-][A-Za-z0-9_.-]*\\.pdf")) && !filename.contains(".."))
            val url = entry.getString("new_download_url")
            SamplePdfEndpoints.requirePdf(url)
            require(URI(url).path.substringAfterLast('/') == filename && urls.add(url))
            val title = entry.getString("title")
            require(title.isNotBlank() && title.length <= 500 && title.none { it.isISOControl() })
            val bytes = positiveLong(entry, "bytes")
            require(bytes <= 512L * 1024 * 1024)
            val pages = positiveLong(entry, "pages")
            require(pages <= 10000)
            val sha = entry.getString("sha256")
            require(sha.matches(Regex("[0-9a-f]{64}")))
            require(entry.getString("license") == "CC-BY-4.0")
            entry.getJSONArray("legacy") // Legacy aliases never control an existing local cache path.
            val previous = bundled[id]
            require(previous == null || previous.languageCode == language && previous.locale == locale)
            SamplePdfItem(id, title, previous?.fileName ?: filename, url,
                String.format(Locale.US, "%.1f MiB", bytes / 1048576.0), language,
                SamplePdfLanguages.displayName(language), bytes, sha, pages.toInt(), locale)
        }
        // A truncated/replaced list must not silently hide any existing sample or selected language.
        require(ids.containsAll(bundled.keys))
        return result
    }

    private fun positiveLong(objectValue: JSONObject, key: String): Long {
        val value = objectValue.get(key)
        require(value is Number && value.toString().matches(Regex("[1-9][0-9]*")))
        return value.toString().toLong()
    }
}

internal object SamplePdfEndpoints {
    fun requirePdf(url: String) {
        val uri = checked(url)
        require(uri.host == "github.com" && uri.rawQuery == null && uri.rawPath == uri.path)
        require(uri.path.matches(Regex("/tyamada/manga-prompt-kit/releases/download/[A-Za-z0-9][A-Za-z0-9._-]*/[A-Za-z0-9_-][A-Za-z0-9_.-]*\\.pdf")))
        require(uri.path.split('/').none { it == "." || it == ".." })
    }

    /** GitHub's signed asset redirects are necessary, but arbitrary HTTPS hosts are not. */
    fun requireRedirect(url: String) {
        val uri = checked(url)
        when (uri.host) {
            "github.com" -> require(uri.path.startsWith("/tyamada/manga-prompt-kit/releases/download/"))
            "raw.githubusercontent.com" -> require(url == SamplePdfManifest.LATEST_URL)
            "release-assets.githubusercontent.com", "objects.githubusercontent.com" -> Unit
            else -> error("Untrusted sample download host")
        }
    }

    private fun checked(url: String): URI = URI(url).also {
        require(it.scheme == "https" && it.rawUserInfo == null && it.rawFragment == null && it.port == -1)
    }
}

/** Android's JSONTokener accepts comments/unquoted keys. Reject those and bound nesting
 * before invoking the platform parser so malformed responses cannot overflow its stack. */
private class StrictManifestJson(private val text: String) {
    private var cursor = 0
    fun validate() { value(0); whitespace(); require(cursor == text.length) }
    private fun whitespace() { while (cursor < text.length && text[cursor] in " \r\n\t") cursor++ }
    private fun take(character: Char): Boolean {
        whitespace()
        return if (cursor < text.length && text[cursor] == character) { cursor++; true } else false
    }
    private fun value(depth: Int) {
        require(depth <= 32)
        whitespace()
        require(cursor < text.length)
        when (text[cursor]) {
            '{' -> {
                cursor++
                val keys = mutableSetOf<String>()
                if (!take('}')) do {
                    whitespace()
                    val rawKey = string()
                    require(keys.add(org.json.JSONTokener(rawKey).nextValue() as String))
                    require(take(':'))
                    value(depth + 1)
                    if (take('}')) return
                    require(take(','))
                } while (true)
            }
            '[' -> {
                cursor++
                if (!take(']')) do {
                    value(depth + 1)
                    if (take(']')) return
                    require(take(','))
                } while (true)
            }
            '"' -> string()
            't' -> literal("true")
            'f' -> literal("false")
            'n' -> literal("null")
            else -> {
                val number = NUMBER.find(text, cursor)
                require(number != null && number.range.first == cursor)
                cursor = number.range.last + 1
            }
        }
    }
    private fun literal(expected: String) { require(text.startsWith(expected, cursor)); cursor += expected.length }
    private fun string(): String {
        val start = cursor
        require(cursor < text.length && text[cursor++] == '"')
        while (cursor < text.length) {
            val character = text[cursor++]
            if (character == '"') return text.substring(start, cursor)
            require(character.code >= 32)
            if (character == '\\') {
                require(cursor < text.length)
                val escape = text[cursor++]
                require(escape in "\"\\/bfnrtu")
                if (escape == 'u') repeat(4) {
                    require(cursor < text.length && text[cursor++] in "0123456789abcdefABCDEF")
                }
            }
        }
        error("Unterminated JSON string")
    }
    companion object { private val NUMBER = Regex("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?") }
}
