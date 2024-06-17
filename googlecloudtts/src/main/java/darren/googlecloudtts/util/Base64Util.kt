package darren.googlecloudtts.util

import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


fun convertBase64ToAudioFile(base64Audio: String, outputPath: String) {
    val audioBytes: ByteArray = Base64.decode(base64Audio, Base64.DEFAULT)

    var fos: FileOutputStream? = null
    try {
        val outputFile = File(outputPath)
        fos = FileOutputStream(outputFile)
        fos.write(audioBytes)
        fos.flush()
        Log.d("AudioConversion", "Audio file saved at: $outputPath")
    } catch (e: IOException) {
        Log.e("AudioConversion", "Error writing audio file", e)
    } finally {
        if (fos != null) {
            try {
                fos.close()
            } catch (e: IOException) {
                Log.e("AudioConversion", "Error closing FileOutputStream", e)
            }
        }
    }
}