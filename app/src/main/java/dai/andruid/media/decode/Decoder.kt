package dai.andruid.media.decode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class Decoder {

    private data class ExtractorInfo(
            var mime: String,
            var track: Int,
    )

    private val decodeThread = Thread { decodeRunnable() }

    var surface: Surface? = null
    var dataSource: String? = null

    private var mediaCodec: MediaCodec? = null
    private var extractor: MediaExtractor? = null
    private val isWorking = AtomicBoolean(false)


    fun start() {
        if (!isWorking.get()) {
            decodeThread.start()
        }
    }

    fun stop() {
    }

    fun release() {
    }


    private fun decodeRunnable() {
    }


    private fun createMediaCodec() {
        if (dataSource.isNullOrEmpty() || null == surface) {
            Log.e(TAG, "Please set data source and surface")
            return
        }

        extractor = createMediaExtractor(dataSource!!)
        if (null == extractor) {
            Log.e(TAG, "create MediaExtractor failed")
            return
        }

        val extractorInfo = matchExtractorInfo(extractor!!, VIDEO)
        if (null == extractorInfo) {
            Log.e(TAG, "match $VIDEO failed")
            return
        }

        val mediaFormat = extractor?.getTrackFormat(extractorInfo.track)
        extractor?.selectTrack(extractorInfo.track)

        try {
            mediaCodec = MediaCodec.createDecoderByType(extractorInfo.mime)
            mediaCodec?.configure(mediaFormat, surface, null, 0)
            mediaCodec?.start()
        } catch (e: IOException) {
            Log.e(TAG, "createDecoderByType ${extractorInfo.mime} failed", e)
        }
    }


    companion object {
        private const val TAG = "Decoder"

        private const val VIDEO = "video/"

        private fun createMediaExtractor(url: String): MediaExtractor? {
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(url)
            } catch (e: IOException) {
                Log.e(TAG, "setDataSource failed $url", e)
                return null
            }
            return extractor
        }

        private fun matchExtractorInfo(extractor: MediaExtractor, target: String): ExtractorInfo? {
            for (i in 0..extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.isNullOrEmpty()) continue

                if (mime.startsWith(target)) {
                    return ExtractorInfo(mime, i)
                }
            }
            return null
        }
    }


}