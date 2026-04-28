package com.kia.sensorhub.data.export

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.kia.sensorhub.data.model.SensorReading
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.io.File

class DataExportManagerJsonVersionTest {

    /**
     * Weryfikuje, że eksport JSON używa wersji aplikacji pobranej runtime z PackageManager,
     * zamiast wcześniej zahardkodowanego stałego stringa.
     */
    @Test
    fun `should export runtime app version in json metadata`() = runTest {
        val runtimeVersion = "9.9.9-test"
        val packageName = "com.kia.sensorhub"
        val tempDir = createTempDir(prefix = "export-json-test")

        val packageInfo = PackageInfo().apply {
            versionName = runtimeVersion
        }

        val packageManager = mockk<PackageManager>()
        every { packageManager.getPackageInfo(packageName, 0) } returns packageInfo

        val context = mockk<Context>()
        every { context.packageName } returns packageName
        every { context.packageManager } returns packageManager
        every { context.filesDir } returns tempDir
        every { context.getExternalFilesDir(null) } returns tempDir

        val manager = DataExportManager(context)
        val result = manager.exportToJson(readings = listOf(SensorReading(timestamp = 1L, sensorType = "ACC")))

        val exportedFile = (result as ExportResult.Success).file
        val exportedJson = JSONObject(exportedFile.readText())

        assertEquals(runtimeVersion, exportedJson.getString("appVersion"))
        assertNotEquals("3.0.0-alpha", exportedJson.getString("appVersion"))

        File(tempDir, "exports").deleteRecursively()
    }
}
