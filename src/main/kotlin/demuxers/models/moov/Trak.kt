package demuxers.models.moov
import demuxers.Box
import demuxers.BoxHeader
import demuxers.SampleInfo
import demuxers.TrakFilter
import demuxers.models.Moof
import demuxers.models.moof.traf.Trun
import demuxers.models.moov.trak.Edts
import demuxers.models.moov.trak.Mdia
import demuxers.models.moov.trak.Tkhd
import demuxers.readBoxHeader
import java.io.RandomAccessFile

class Trak private constructor(
    header: BoxHeader,
    private val mvhdTimeScale: Long?,
    val doLogging: Boolean,
    val trakFilter: TrakFilter?=null,
    val reader: RandomAccessFile,
    val endOffset: Long,
    startOffset: Long,

) : Box(header) {

    var tkhd: Tkhd? = null
    var edts: Edts?=null
    var mdia: Mdia?=null
    var nextMoofOffset = startOffset
    var initial=true


    private var trunsInUse =mutableListOf<Trun>()

    override fun parse(reader: RandomAccessFile) {

        reader.seek(header.payloadOffset)

        while (reader.filePointer < header.offset + header.size) {

            val childHeader = readBoxHeader(reader) ?: break

            when(childHeader.type) {

                "tkhd" -> {
                    tkhd = Tkhd.getBox(childHeader, reader,mvhdTimeScale,doLogging)
                }
                "edts" -> {
                    edts = Edts.getBox(childHeader, reader,doLogging)
                }
                "mdia" -> {
                    mdia = Mdia.getBox(childHeader, reader,doLogging,trakFilter)
                }
            }

            reader.seek(childHeader.offset + childHeader.size)
        }
    }

    fun getTkhdAsBytes(reader: RandomAccessFile): ByteArray? {
        val tkhdBox = tkhd ?: return null

        val size = tkhdBox.header.size.toInt()
        if (size <= 0) return null

        val bytes = ByteArray(size)
        reader.seek(tkhdBox.header.offset)
        reader.readFully(bytes)

        return bytes
    }

    fun getEdtsAsBytes(reader: RandomAccessFile): ByteArray? {
        val tkhdBox = edts ?: return null
        val size = edts!!.header.size.toInt()
        if (size <= 0) return null

        val bytes = ByteArray(size)
        reader.seek(tkhdBox.header.offset)
        reader.readFully(bytes)

        return bytes
    }

    fun getSamples(reader: RandomAccessFile,requestedSampleCount: Int): MutableList<SampleInfo>{
        if (trunsInUse.isEmpty()){
            reader.seek(nextMoofOffset)
            while (reader.filePointer < endOffset) {
                val header = readBoxHeader(reader) ?: break
                if (header.type=="moof"){
                    val moofEnd = header.offset + header.size
                    nextMoofOffset = moofEnd

                    trunsInUse= Moof.getTruns(header,reader,tkhd?.trackId)
                    if (trunsInUse.isNotEmpty()){
                        val trun = trunsInUse.first()
                        val samps = trun.getSamplesInfo(
                            reader,
                            requestedSampleCount,
                            mdia?.hdlr?.handlerType
                        )
                        if (trun.samplesRetrivedCont >= trun.sampleCount) {
                            trunsInUse.removeFirst()
                        }
                        return samps
                    }
                }
                reader.seek(header.offset + header.size)
            }
        }else{

            val trun = trunsInUse.first()

            val samps = trun.getSamplesInfo(
                reader,
                requestedSampleCount,
                mdia?.hdlr?.handlerType
            )

            if (trun.samplesRetrivedCont >= trun.sampleCount) {
                trunsInUse.removeFirst()
            }

            return samps
        }
        return mutableListOf()


    }

    fun writeFutureTrak(output: RandomAccessFile,reader: RandomAccessFile){
        val trakStart = output.filePointer
        output.writeInt(0)
        output.write("trak".toByteArray(Charsets.US_ASCII))

        val tkhd=getTkhdAsBytes(reader)!!
        output.write(tkhd)
        val edts=getEdtsAsBytes(reader)
        if (edts!=null){
            output.write(edts)
        }
        mdia?.writeFutureMdia(output,reader)

        val trakEnd = output.filePointer
        val trakSize = (trakEnd - trakStart).toInt()
        output.seek(trakStart)
        output.writeInt(trakSize)
        output.seek(trakEnd)
    }
    fun writeSamplesTo(output: RandomAccessFile, reader: RandomAccessFile): Int {

        val requested = if (initial) 2 else 6
        val samps = mutableListOf<SampleInfo>()

        while (samps.size < requested) {
            val more = getSamples(reader, requested - samps.size)

            if (more.isEmpty()) {
                break
            }

            samps.addAll(more)
        }

        if (samps.isEmpty()) {
            return 0
        }
        val writerPos=output.filePointer
        mdia?.minf?.stbl?.stco?.writeStcoEntry(output, writerPos.toInt())

        for ((index, samp) in samps.withIndex()) {
            if (samp.absOffset != null) {
                val frameData = ByteArray(samp.size.toInt())
                reader.seek(samp.absOffset)
                reader.readFully(frameData)
                output.write(frameData)
            }
            mdia?.minf?.stbl?.stsz?.writeStszEntry(output,samp.size.toInt())
            if (samp.isKeyFrame!=null){
                if (samp.isKeyFrame){
                    val sampleNmber=mdia?.minf?.stbl?.stsz?.entryIndex?:0
                    mdia?.minf?.stbl?.stss?.writeStssEntry(output,sampleNmber)
                }
            }

            if (samp.ctts!=null){
                mdia?.minf?.stbl?.ctts?.writePreviousCttsEntry(
                    output,
                    samp.ctts.toInt()
                )
                // Flush after the last sample only if this is the final batch.
                if (index == samps.lastIndex && samps.size != 2 && samps.size != 6) {
                    mdia?.minf?.stbl?.ctts?.writePreviousCttsEntry(output,null)
                }



            }

        }


        initial = false

        return samps.size
    }



    companion object {
        fun getBox(
            header: BoxHeader,
            reader: RandomAccessFile,
            mvhdTimeScalep: Long?,
            doLogging: Boolean,
            trakFilter: TrakFilter?=null,
            endOffset: Long,
            startOffset: Long,
        ): Trak? {
            return try {
                val tk=Trak(header,mvhdTimeScalep,doLogging,trakFilter,reader,endOffset,startOffset)
                tk.parse(reader)
                if (tk.mdia!=null){
                    return tk
                }
                return null
            } catch (e: Exception) {
                null
            }
        }
    }
}