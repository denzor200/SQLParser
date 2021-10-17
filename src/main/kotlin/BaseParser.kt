import interfaces.IFrom

abstract class BaseParser<T> {
    abstract fun parse(t: SQLTokenizer): T?

    fun parseExpected(t: SQLTokenizer): T
    {
        val obj = parse(t)
        if (obj==null)
        {
            // TODO: throw expectation failure
        }
        return obj!!
    }
}

abstract class BaseParserVoid {
    abstract fun parse(t: SQLTokenizer): Boolean

    fun parseExpected(t: SQLTokenizer)
    {
        if (!parse(t))
        {
            // TODO: throw expectation failure
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

