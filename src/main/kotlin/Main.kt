import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.encodeToString

internal object SQLParserKt {
    @JvmStatic
    fun main(args: Array<String>) {
        while (true)
        {
            print("SQLParser> ")
            val line = readLine()
            if (line==null)
                break
            else
            {
                try {
                    val format = Json { prettyPrint = true }
                    var q = Query()
                    q.parse(line)
                    println("Parsed: " + format.encodeToString(Json.encodeToJsonElement(q)))
                }
                catch (e: QueryError)
                {
                    println("Error: " + e.message)
                }
            }
        }
    }
}