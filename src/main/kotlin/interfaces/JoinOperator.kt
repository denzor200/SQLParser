package interfaces

import kotlinx.serialization.Serializable
import BaseParser
import KeywordParser
import SQLTokenizer

@Serializable
enum class JoinOperator
{
    INNER, LEFT, RIGHT, FULL
}

class JoinOperatorParser : BaseParser<JoinOperator>()
{
    override fun parse(t: SQLTokenizer): JoinOperator?
    {
        if (KeywordParser(SQLTokens.INNER).parse(t))
        {
            KeywordParser(SQLTokens.JOIN).parseExpected(t)
            return JoinOperator.INNER
        }
        else if (KeywordParser(SQLTokens.LEFT).parse(t))
        {
            KeywordParser(SQLTokens.JOIN).parseExpected(t)
            return JoinOperator.LEFT
        }
        else if (KeywordParser(SQLTokens.RIGHT).parse(t))
        {
            KeywordParser(SQLTokens.JOIN).parseExpected(t)
            return JoinOperator.RIGHT
        }
        else if (KeywordParser(SQLTokens.FULL).parse(t))
        {
            KeywordParser(SQLTokens.JOIN).parseExpected(t)
            return JoinOperator.FULL
        }
        return null
    }
}
