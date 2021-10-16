import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import interfaces.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.encodeToJsonElement

class Test
{
    @Test fun example()
    {
        val expected = """
            {
               "columns":[
                  {
                     "type":"interfaces.ColumnStar"
                  }
               ],
               "from":{
                  "type":"interfaces.FromTableOrSubqueryList",
                  "tableOrSubqueryList":[
                     {
                        "type":"interfaces.TableName",
                        "tableName":"book"
                     }
                  ]
               }
            }
        """.trimIndent()
        var q = Query()
        q.parse("SELECT * FROM book")
        assertEquals(Json.decodeFromString(expected), Json.encodeToJsonElement(q))
    }
}

