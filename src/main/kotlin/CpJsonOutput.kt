import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import interfaces.TableOutput
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.Exception

class CpJsonOutput(private val specifiedModuleName:String) : TableOutput {
    override fun readAndModify(nls: Map<NlsMap, Map<String, String>>) {
        val sourceFolder = "cp-json-src"
        val resultFolder = "cp-json-result"
        val defaultModuleName = "module"
        val files = File(sourceFolder).listFiles()
        var moduleName: String = defaultModuleName
        val jsonParser = JsonParser()
        val gson = GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
        for (item in nls) {
            //read the json file
            var targetFolder: File? = null
            files?.run {
                for (file in this) {
                    if (file.isDirectory) {
                        if (item.key.outputCode.contains("HASE", true)) {
                            if (file.nameWithoutExtension.endsWith(item.key.outputCode)) {
                                targetFolder = file
                                break
                            }
                        } else {
                            if (file.nameWithoutExtension.endsWith(item.key.outputCode) && !file.nameWithoutExtension.contains(
                                    "HASE",
                                    true
                                )
                            ) {
                                targetFolder = file
                                break
                            }
                        }
                    }
                }
            }
            var targetFile: File? = null
            var existingJson = if (targetFolder != null && targetFolder?.exists() == true) {
                val nlsFiles = targetFolder?.listFiles() ?: emptyArray()
                if (specifiedModuleName.isNullOrBlank() && nlsFiles.size > 1) {
                    throw Exception("Can not judge which file is the target file in ${sourceFolder}${File.separator}$targetFolder}")
                } else if (nlsFiles.size == 1) {
                    targetFile = nlsFiles[0]
                    moduleName = targetFile.nameWithoutExtension
                    jsonParser.parse(FileReader(targetFile)).asJsonObject
                } else if (!specifiedModuleName.isNullOrBlank() && nlsFiles.size > 1) {
                    var existsTargetModuleFile = false
                    moduleName = specifiedModuleName
                    for (f in nlsFiles) {
                        if (specifiedModuleName == f.nameWithoutExtension) {
                            existsTargetModuleFile = true
                            targetFile = f
                        }
                    }
                    if (existsTargetModuleFile) {
                        jsonParser.parse(FileReader(targetFile)).asJsonObject
                    } else {
                        JsonObject()
                    }
                } else {
                    JsonObject()
                }
            } else {
                JsonObject()
            }

            //modify json
            var srcFileNeedChange = false
            nls[item.key]?.run {
                for (nls in this) {
                    val inputLabel = nls.key
                    val inputValue = valueTransform(nls.value)
                    if (inputLabel.isNotBlank() && inputValue.isNotBlank()) {
                        //update or add the label
                        if (existingJson.has(inputLabel)) {
                            existingJson.remove(inputLabel)
                        }
                        existingJson.addProperty(inputLabel, inputValue)
                        srcFileNeedChange = true
                    }
                }
            }
            // judge if this language nls file has change
            if (srcFileNeedChange) {
                //write json
                val resultNlsFolder = "$resultFolder${File.separator}${item.key.outputCode}"
                val outputJsonFilePath =
                    "$resultNlsFolder${File.separator}${targetFile?.name ?: "$moduleName.json"}"
                val resultNlsFolderFile = File(resultNlsFolder)
                if (!resultNlsFolderFile.exists()) resultNlsFolderFile.mkdirs()
                // 指定文本的写出的格式：
                val writer = FileWriter(outputJsonFilePath)
                gson.toJson(existingJson, writer)
                writer.flush() //flush data to file
                writer.close() //close writer
            }
        }
    }

    override fun valueTransform(src: String): String {
        return src.trimEnd()
    }
}