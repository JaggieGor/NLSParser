import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedInputStream
import java.io.FileInputStream

object NLSWorkbookReader {
    fun readExcel(path: String, mappings: Array<NlsMap>): Map<NlsMap, Map<String, String>> {
        val fileInputStream = FileInputStream(path)
        val bufferedInputStream = BufferedInputStream(fileInputStream)
        val nlsWorkbook = XSSFWorkbook(bufferedInputStream)
        val nlsSheet = nlsWorkbook.getSheetAt(0)
        val lastRowIndex: Int = nlsSheet.lastRowNum
        println(lastRowIndex)

        //parse the label column and language
        val headerRow = nlsSheet.getRow(0)
        var nlsKeyColumn: Int = 0
        val columnMap = mutableMapOf<NlsMap, Int>()
        for (k in 0..headerRow.lastCellNum) {
            headerRow.getCell(k)?.run {
                val cellValue = getStringValueForCell(this)
                val strings = cellValue.split(":")
                val formattedCellValue =
                    if (strings.size == 1) cellValue else "${strings[0]?.trim() ?: ""}:${strings[1]?.trim() ?: ""}"
                if (formattedCellValue.contains(
                        "key",
                        true
                    ) || formattedCellValue.contains("label", true)
                ) {
                    nlsKeyColumn = k
                } else {
                    for (nlsMap in mappings) {
                        if (formattedCellValue?.startsWith("${nlsMap.oCode}:${nlsMap.remark}", true)) {
                            columnMap[nlsMap] = k
                        } else if (!columnMap.contains(nlsMap) && formattedCellValue?.contains(nlsMap.remark, true)) {
                            columnMap[nlsMap] = k
                        }
                    }
                }
            }
        }

//    println("nls key column: $nlsKeyColumn")
//    for (item in columnMap) {
//        println("${item.key.oCode}, column ${item.value}")
//    }

        // read the label and nls
        val nls = mutableMapOf<NlsMap, MutableMap<String, String>>()
        for (item in columnMap) {
            val labelMap = mutableMapOf<String, String>()
            for (i in 1..lastRowIndex) {
                nlsSheet.getRow(i).run {
                    val label = getStringValueForCell(getCell(nlsKeyColumn))
                    val value = getStringValueForCell(getCell(item.value))
                    labelMap[label] = value
                }
            }
            nls[item.key] = labelMap
        }

        //print all en nls
//    nls[(LanguageUtil.androidXmlMappings)[0]]?.run {
//        for (item in this) {
//            println("label: ${item.key}, value: ${item.value}")
//        }
//    }

        return nls

//    for (i in 0..lastRowIndex) {
//        val row = nlsSheet.getRow(i) ?: break
//        val lastCellNum = row.lastCellNum
//        for (j in 0 until lastCellNum) {
//            row.getCell(j)?.run {
//                when (cellType) {
//                    CellType.STRING -> println(stringCellValue)
//                    CellType.NUMERIC -> println(numericCellValue)
//                    CellType.BLANK -> println(" ")
//                    CellType.BOOLEAN -> println(booleanCellValue)
//                }
//            }
//        }
//    }
    }

    private fun getStringValueForCell(cell: XSSFCell?): String {
        return cell?.run {
            when (cellType) {
                CellType.STRING -> stringCellValue
                CellType.NUMERIC -> numericCellValue.toString()
                CellType.BOOLEAN -> booleanCellValue.toString()
                CellType.ERROR -> "Cell Error"
                CellType.FORMULA -> rawValue
                else -> ""
            }
        } ?: ""
    }
}