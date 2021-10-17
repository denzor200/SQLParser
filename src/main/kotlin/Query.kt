import interfaces.*
import kotlinx.serialization.Serializable

@Serializable class Query {
    var stmt = SelectStmt()

    fun parse(str: String)
    {
        var t = SQLTokenizer(str)
        stmt = SelectStmtParser().parseExpected(t) as SelectStmt
    }
};
