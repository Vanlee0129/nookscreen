package app.orchard.tv.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import kotlin.coroutines.coroutineContext

internal object UpdateDownload {
    private const val MAX_ATTEMPTS = 3
    private const val STALL_MS = 30_000L
    private const val STALL_BYTES = 256 * 1024L
    private val contentRange = Regex("""bytes (\d+)-(\d+)/(\d+)""")

    /** Keep an unfinished file only for retries of this request. Callers verify the finished APK. */
    suspend fun download(
        file: File,
        connect: () -> HttpURLConnection,
        elapsedMs: () -> Long,
        progress: suspend (Int) -> Unit,
        retrying: (Long, IOException) -> Unit,
    ) {
        val directory = checkNotNull(file.parentFile)
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create update directory: $directory" }
        val part = File(directory, file.name + ".part")
        if (part.exists() && !part.delete()) throw IOException("Could not clear stale update download")
        var total = -1L
        var validator: String? = null

        try {
            var attempt = 0
            while (true) {
                coroutineContext.ensureActive()
                val offset = part.length()
                val connection = connect()
                try {
                    connection.setRequestProperty("Accept-Encoding", "identity")
                    if (offset > 0) {
                        connection.setRequestProperty("Range", "bytes=$offset-")
                        validator?.let { connection.setRequestProperty("If-Range", it) }
                    }
                    val code = connection.responseCode
                    val length = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
                    val etag = connection.getHeaderField("ETag")?.takeUnless { it.startsWith("W/") }
                    val append = code == HttpURLConnection.HTTP_PARTIAL && offset > 0
                    val start = if (append) offset else 0L
                    when {
                        append -> {
                            val range = contentRange.matchEntire(connection.getHeaderField("Content-Range").orEmpty())
                            val first = range?.groupValues?.get(1)?.toLongOrNull()
                            val last = range?.groupValues?.get(2)?.toLongOrNull()
                            val size = range?.groupValues?.get(3)?.toLongOrNull()
                            if (first != offset || size == null || size <= offset || last != size - 1 ||
                                length >= 0 && length != size - offset || total >= 0 && total != size ||
                                validator != null && etag != null && validator != etag
                            ) throw IOException("Inconsistent update Content-Range")
                            total = size
                        }
                        // A server that ignores Range or sees a different validator sends the whole file.
                        code == HttpURLConnection.HTTP_OK -> { total = length; validator = etag }
                        else -> throw IOException("HTTP $code")
                    }
                    transfer(connection, part, append, start, total, elapsedMs, progress)
                    break
                } catch (e: CancellationException) {
                    throw e
                } catch (e: IOException) {
                    val downloaded = part.length()
                    if (++attempt >= MAX_ATTEMPTS || downloaded <= offset) throw e
                    retrying(downloaded, e)
                } finally {
                    connection.disconnect()
                }
            }
            coroutineContext.ensureActive()
            if (!part.renameTo(file)) throw IOException("Could not keep the update download")
        } finally {
            part.delete()
        }
    }

    private suspend fun transfer(
        connection: HttpURLConnection,
        part: File,
        append: Boolean,
        start: Long,
        total: Long,
        elapsedMs: () -> Long,
        progress: suspend (Int) -> Unit,
    ) {
        var done = start
        var windowAt = elapsedMs()
        var windowFrom = start
        var shown = -1
        // Open the file first so a failed full response cannot leave bytes from the old representation.
        FileOutputStream(part, append).use { output ->
            connection.inputStream.use { input ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    coroutineContext.ensureActive()
                    val count = input.read(buffer)
                    if (count < 0) break
                    if (total >= 0 && count > total - done) throw IOException("Update exceeds response length")
                    output.write(buffer, 0, count)
                    done += count
                    val now = elapsedMs()
                    if (now - windowAt >= STALL_MS) {
                        if (done - windowFrom < STALL_BYTES) throw IOException("Update download stalled")
                        windowAt = now
                        windowFrom = done
                    }
                    val percent = if (total > 0) (done.toDouble() / total * 100).toInt() else 0
                    if (percent != shown) {
                        shown = percent
                        progress(percent)
                    }
                }
            }
        }
        if (done == 0L || total >= 0 && done != total) throw IOException("Incomplete update download")
    }
}
