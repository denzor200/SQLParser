import ast.*
import kotlinx.serialization.Serializable

@Serializable class Query {
    var stmt = SelectStmt()

    fun parse(str: String)
    {
        var t = SQLTokenizer(str)
        stmt = SelectStmtParser().parseExpected(t) as SelectStmt
        SymParser(SQLTokens.Symbolized.SYM_SEMICOLON).parseExpected(t)

        // Проверяем что все токены исчерпаны
        if (t.hasMoreTokens())
        {
            throw SyntaxError(t.character())
        }
    }
};

open class QueryError(message: String) : Throwable(message)
{
}

class SyntaxError : QueryError
{
    constructor(ch: String) : super("near \"$ch\": syntax error")
    constructor(ch: Char) : this(ch.toString())
}

class EoiError() : QueryError("end of input")
{
}
