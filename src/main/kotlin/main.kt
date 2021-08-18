import java.lang.Exception

fun main(args: Array<String>) {
//    val nls = NLSWorkbookReader.readExcel(args[0] ?: "", LanguageUtil.androidXmlMappings)
//    AndroidXmlOutput.readAndModify(nls)
//    val nls = NLSWorkbookReader.readExcel(args[0] ?: "", LanguageUtil.iosJsonMappings)
//    IOSJsonOutput.readAndModify(nls)
    val nlsFilePath = try {
        args[0]
    } catch (e: Exception) {
        "template.xlsx"
    }
    val moduleName = try {
        args[1]
    } catch (e: Exception) {
        ""
    }
    val nlsForAndroidXml = NLSWorkbookReader.readExcel(nlsFilePath, LanguageUtil.androidXmlMappings)
    AndroidXmlOutput.readAndModify(nlsForAndroidXml)
    val nlsForIOSJson = NLSWorkbookReader.readExcel(nlsFilePath, LanguageUtil.iosJsonMappings)
    IOSJsonOutput.readAndModify(nlsForIOSJson)
    val nlsForCpJson = NLSWorkbookReader.readExcel(nlsFilePath, LanguageUtil.cpJsonMappings)
    CpJsonOutput(moduleName).readAndModify(nlsForCpJson)
}
