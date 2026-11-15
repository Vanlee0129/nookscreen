package app.orchard.tv.update

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdateDownloadTest {
    @get:Rule val temporary = TemporaryFolder()
    private val apk = "1234567890".toByteArray()

    @Test fun interruptedResponseResumesAtTheExactOffset() {
        val first = Response(200, apk.copyOf(4), length = "10", etag = "\"release\"")
        val rest = Response(206, apk.copyOfRange(4, 10), range = "bytes 4-9/10", etag = "\"release\"")
        val output = download(first, rest)
        assertArrayEquals(apk, output)
        assertNull(first.getRequestProperty("Range"))
        assertEquals("bytes=4-", rest.getRequestProperty("Range"))
        assertEquals("\"release\"", rest.getRequestProperty("If-Range"))
    }

    @Test fun aFullResponseToRangeReplacesThePartialFile() {
        val first = Response(200, apk.copyOf(4), length = "10")
        val full = Response(200, apk)
        assertArrayEquals(apk, download(first, full))
        assertEquals("bytes=4-", full.getRequestProperty("Range"))
    }

    @Test fun mismatchedContentRangeNeverPublishesAnApk() {
        val first = Response(200, apk.copyOf(4), length = "10")
        val wrong = Response(206, apk.copyOfRange(3, 10), range = "bytes 3-9/10")
        val target = temporary.root.resolve("update.apk")
        val failure = runCatching { download(first, wrong) }.exceptionOrNull()
        assertEquals("Inconsistent update Content-Range", (failure as IOException).message)
        assertFalse(target.exists())
        assertFalse(temporary.root.resolve("update.apk.part").exists())
    }

    private fun download(vararg responses: Response): ByteArray = runBlocking {
        val pending = responses.iterator()
        val target = temporary.root.resolve("update.apk")
        UpdateDownload.download(target, { pending.next() }, { 0L }, {}, { _, _ -> })
        target.readBytes()
    }

    private class Response(
        private val status: Int,
        private val body: ByteArray,
        private val length: String = body.size.toString(),
        private val range: String? = null,
        private val etag: String? = null,
    ) : HttpURLConnection(URL("https://example.invalid/update.apk")) {
        override fun getResponseCode() = status
        override fun getHeaderField(name: String) = when (name) {
            "Content-Length" -> length
            "Content-Range" -> range
            "ETag" -> etag
            else -> null
        }
        override fun getInputStream() = body.inputStream()
        override fun connect() = Unit
        override fun disconnect() = Unit
        override fun usingProxy() = false
    }
}
