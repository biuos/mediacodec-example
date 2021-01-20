package dai.android.app.media.decode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import dai.android.app.media.SpeedController
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

// Android——使用MediaExtractor获取关键帧的时间戳
// https://blog.csdn.net/m0_37602827/article/details/109302139

// MediaCodec解码aac,播放
// https://blog.csdn.net/coloriy/article/details/60579451


// MediaCodec 解析MP4 并自行播放video和audio
// https://www.jianshu.com/p/b625dba0399a?utm_campaign=maleskine&utm_content=note&utm_medium=seo_notes&utm_source=recommendation

//

class Decoder1 {

    private data class ExtractorInfo(
            var mime: String,
            var track: Int,
    )

    private val decodeThread = Thread { decodeRunnable() }
    private val speedController = SpeedController()

    var surface: Surface? = null
    var dataSource: String? = null

    private var mediaCodec: MediaCodec? = null
    private var extractor: MediaExtractor? = null
    private val isWorking = AtomicBoolean(false)


    fun start() {
        if (!isWorking.get()) {
            isWorking.set(true)
            decodeThread.start()
        }
    }

    fun stop() {
        if (isWorking.get()) {
            isWorking.set(false);
        }
    }


    private fun decodeRunnable() {
        // create the MediaCodec first
        if (null == mediaCodec) {
            createMediaCodec()
        }
        // check again
        if (null == mediaCodec || null == extractor) {
            Log.e(TAG, "create MediaExtractor and MediaCodec failed")
            return
        }

        var startTime: Long = System.currentTimeMillis()
        val bufferInfo = MediaCodec.BufferInfo()
        var eof = false

        while (true) {
            if (null == mediaCodec || null == extractor) {
                Log.e(TAG, "MediaCodec or MediaExtractor is null")
                break
            }

            if (!eof) {
                val inputIndex: Int = mediaCodec!!.dequeueInputBuffer(10000)
                if (inputIndex < 0) {
                    //Log.w(TAG, "dequeueInputBuffer failed index=$inputIndex")
                } else {
                    val buffer: ByteBuffer? = mediaCodec!!.getInputBuffer(inputIndex)
                    if (null != buffer) {
                        val sampleSize = extractor!!.readSampleData(buffer, 0)
                        if (sampleSize < 0) {
                            mediaCodec!!.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            eof = true
                            Log.i(TAG, "queueInputBuffer: End of stream")
                        } else {
                            // 读取下一帧后必须调用提取下一帧
                            // 控制帧率在 30帧左右
                            // 读取时间戳
                            val time = extractor!!.sampleTime
                            if(time >  0) {
                                speedController.preRender(time)
                            }
                            mediaCodec!!.queueInputBuffer(inputIndex, 0, sampleSize, extractor!!.sampleTime, 0)
                            extractor!!.advance()
                        }

                    } else {
                        Log.w(TAG, "getInputBuffer($inputIndex) failed");
                    }
                }
            }

            val outputIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 10000)
            if (outputIndex < 0) {
                when (outputIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.w(TAG, "Output format changed, new format:${mediaCodec?.outputFormat}")
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.w(TAG, "dequeueOutputBuffer: Time out, try again")
                    }
                    else -> {
                        Log.w(TAG, "dequeueOutputBuffer: unknown failed, index=$outputIndex")
                    }
                }
            }
            // good data
            else {
                val byteBuffer = mediaCodec!!.getOutputBuffer(outputIndex)
                if (null == byteBuffer) {
                    Log.w(TAG, "getOutputBuffer($outputIndex) failed")
                } else {
                    mediaCodec?.releaseOutputBuffer(outputIndex, true)
                }
            }

            // All decoded frames have been rendered, we can stop playing now

            // All decoded frames have been rendered, we can stop playing now
            if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                break
            }

            if (!isWorking.get()) {
                break
            }
        }


        // release the resource
        extractor?.release()
        extractor = null

        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null
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
                Log.e(TAG, "mime=$mime")
                if (mime.isNullOrEmpty()) continue

                if(mime.startsWith("audio/")) {
                    Log.e(TAG, "audio found")
                }

                if (mime.startsWith(target)) {
                    return ExtractorInfo(mime, i)
                }
            }
            return null
        }
    }


}