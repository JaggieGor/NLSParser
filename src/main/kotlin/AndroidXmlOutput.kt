import interfaces.TableOutput
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.io.OutputFormat
import org.dom4j.io.SAXReader
import org.dom4j.io.XMLWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object AndroidXmlOutput : TableOutput {
    private val specialCharsMappings = mutableMapOf(
        "\'" to "\\\'",
        "\"" to "\\\"",
        "&" to "&amp;",
        "<" to "&lt;",
        ">" to "&gt;",
        "\n" to "\\n"
    )

    override fun readAndModify(nls: Map<NlsMap, Map<String, String>>) {
        val sourceFolder = "android-xml-src"
        val resultFolder = "android-xml-result"
        // 创建saxReader对象
        val reader = SAXReader()
        for (item in nls) {
            //read the xml file
            val xmlFilePath =
                "$sourceFolder${File.separator}values-${item.key.outputCode}${File.separator}localised_strings.xml"
            val existingXmlFile = File(xmlFilePath)
            var document: Document
            var rootElement: Element
            if (!existingXmlFile.exists()) {
                document = DocumentHelper.createDocument()
                rootElement = document.addElement("resources")
            } else {
                document = reader.read(FileInputStream(xmlFilePath))
                rootElement = document.rootElement
            }
            //modify xml
            var srcFileNeedChange = false
            nls[item.key]?.run {
                for (nls in this) {
                    val inputLabel = nls.key
                    val inputValue = valueTransform(nls.value)
                    var isNewLabel = true
                    for (record in rootElement.elements()) {
                        val existingLabel = record.attribute("name").stringValue
                        if (existingLabel == inputLabel) {
                            record.text = inputValue
                            isNewLabel = false
                            srcFileNeedChange = true
                            break
                        }
                    }
                    if (isNewLabel && inputLabel.isNotBlank() && inputValue.isNotBlank()) {
                        val newElement = rootElement.addElement("string")
                        newElement.addAttribute("name", inputLabel)
                        newElement.text = inputValue
                        srcFileNeedChange = true
                    }
                }
            }

            //write xml
            if (srcFileNeedChange) {
                val nlsFolderPath = "$resultFolder${File.separator}values-${item.key.outputCode}"
                val outputXmlFilePath = "$nlsFolderPath${File.separator}localised_strings.xml"
                val file = File(nlsFolderPath)
                if (!file.exists()) file.mkdirs()
                val outputStream = FileOutputStream(outputXmlFilePath)
                // 指定文本的写出的格式：
                val format = OutputFormat.createPrettyPrint()//漂亮格式：有空格换行
                format.encoding = "UTF-8"
                //1.创建写出对象
                val writer: XMLWriter = XMLWriter(outputStream, format)
                //2.写出Document对象
                writer.write(document)
                //3.关闭流
                writer.close()
            }
        }
    }

    override fun valueTransform(src: String): String {
        var result = src.trimEnd()
        for (item in specialCharsMappings) {
            result = result.replace(item.key, item.value)
        }
        return result
    }
}