import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import interfaces.TableOutput
import java.io.*


object IOSJsonOutput : TableOutput {
//    private val specialCharsMappings = mutableMapOf(
//        "\'" to "\\\'",
//        "\"" to "\\\"",
//        "&" to "&amp;",
//        "<" to "&lt;",
//        ">" to "&gt;",
//        "\n" to "\\n"
//    )

    override fun readAndModify(nls: Map<NlsMap, Map<String, String>>) {
        val sourceFolder = "ios-json-src"
        val resultFolder = "ios-json-result"
        val defaultModuleName = "Module_"
        val files = File(sourceFolder).listFiles()
        var moduleName: String
        val jsonParser = JsonParser()
        val gson = GsonBuilder()
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
        for (item in nls) {
            //read the json file
            var targetFile: File? = null
            files?.run {
                for (file in this) {
                    if (item.key.outputCode.contains("HASE", true)) {
                        if (file.nameWithoutExtension.endsWith(item.key.outputCode)) {
                            targetFile = file
                            break
                        }
                    } else {
                        if (file.nameWithoutExtension.endsWith(item.key.outputCode) && !file.nameWithoutExtension.contains(
                                "HASE",
                                true
                            )
                        ) {
                            targetFile = file
                            break
                        }
                    }
                }
            }
            moduleName = targetFile?.nameWithoutExtension?.replace(item.key.outputCode, "") ?: defaultModuleName
            var existingJson = if (targetFile != null) {
                jsonParser.parse(FileReader(targetFile)).asJsonObject
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
                val outputJsonFilePath =
                    "$resultFolder${File.separator}${targetFile?.name ?: moduleName + item.key.outputCode + ".json"}"
                val file = File(resultFolder)
                if (!file.exists()) file.mkdirs()
                // 指定文本的写出的格式：
                val writer = FileWriter(outputJsonFilePath)
                gson.toJson(existingJson, writer)
                writer.flush() //flush data to file
                writer.close() //close write
            }
        }
    }

    override fun valueTransform(src: String): String {
        return src.trimEnd()
    }
}