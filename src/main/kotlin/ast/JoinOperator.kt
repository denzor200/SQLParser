package ast

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
        if (KeywordParser(SQLTokens.Keywords.INNER).parse(t))
        {
            KeywordParser(SQLTokens.Keywords.JOIN).parseExpected(t)
            return JoinOperator.INNER
        }
        else if (KeywordParser(SQLTokens.Keywords.LEFT).parse(t))
        {
            KeywordParser(SQLTokens.Keywords.JOIN).parseExpected(t)
            return JoinOperator.LEFT
        }
        else if (KeywordParser(SQLTokens.Keywords.RIGHT).parse(t))
        {
            KeywordParser(SQLTokens.Keywords.JOIN).parseExpected(t)
            return JoinOperator.RIGHT
        }
        else if (KeywordParser(SQLTokens.Keywords.FULL).parse(t))
        {
            KeywordParser(SQLTokens.Keywords.JOIN).parseExpected(t)
            return JoinOperator.FULL
        }
        return null
    }
}

class JoinConstraintParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        if (!KeywordParser(SQLTokens.Keywords.ON).parse(t))
            return null
        return ExpressionParser().parseExpected(t)
    }
}