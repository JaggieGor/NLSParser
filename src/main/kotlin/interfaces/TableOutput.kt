package interfaces

import NlsMap

interface TableOutput {
    fun readAndModify(nls: Map<NlsMap, Map<String, String>>)
    fun valueTransform(src: String):String
}