abstract class BaseParser<T> {
    abstract fun parse(t: SQLTokenizer): T?

    fun parseExpected(t: SQLTokenizer): T {
        return parse(t) ?: throw SyntaxError(t.character())
    }
}

abstract class BaseParserVoid {
    abstract fun parse(t: SQLTokenizer): Boolean

    fun parseExpected(t: SQLTokenizer)
    {
        if (!parse(t))
        {
            throw SyntaxError(t.character())
        }
    }
}

class KeywordParser(val kw: String) : BaseParserVoid()
{
    override fun parse(t: SQLTokenizer): Boolean
    {
        val pos = t.getPosition()
        if (t.hasMoreTokens() && t.nextToken() == kw)
            return true
        t.restorePosition(pos)
        return false
    }
}

class SymParser(val sym: Char) : BaseParserVoid()
{
    override fun parse(t: SQLTokenizer): Boolean
    {
        val pos = t.getPosition()
        if (t.hasMoreTokens() && t.nextToken() == sym.toString())
            return true
        t.restorePosition(pos)
        return false
    }
}

class IdentifierParser : BaseParser<String>()
{
    override fun parse(t: SQLTokenizer): String?
    {
        val pos = t.getPosition()
        if (t.hasMoreTokens() && SQLTokens.isIdentifier(t.nextToken()))
            return t.lastToken()
        t.restorePosition(pos)
        return null
    }
}

class IntegerLiteralParser : BaseParser<String>()
{
    override fun parse(t: SQLTokenizer): String?
    {
        val pos = t.getPosition()
        if (t.hasMoreTokens() && SQLTokens.isIntegerLiteral(t.nextToken()))
            return t.lastToken()
        t.restorePosition(pos)
        return null
    }
}

