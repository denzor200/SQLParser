package interfaces

import kotlinx.serialization.Serializable
import SQLTokenizer
import BaseParser
import SymParser
import KeywordParser

@Serializable sealed class IExpression {
}

@Serializable data class ExpressionOperation(var tok: String, var operand: IExpression)

// TODO: rename ExpressionBinary
@Serializable data class ExpressionIntegralLiteral(var value: Int) : IExpression()
@Serializable data class ExpressionIdentifier(var value: String) : IExpression()
@Serializable data class ExpressionFunctionCall(var value: String, var args: MutableList<IExpression> = mutableListOf()) : IExpression()
@Serializable data class ExpressionFunctionCallStar(var value: String) : IExpression()
@Serializable data class ExpressionBinary(var first: IExpression, var rest: MutableList<ExpressionOperation> = mutableListOf()) : IExpression()



class ExpressionBaseParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val fcs = parseExpressionFunctionCallStar(t)
        if (fcs != null)
            return fcs

        val fc = parseExpressionFunctionCall(t)
        if (fc != null)
            return fc

        val id = parseExpressionIdentifier(t)
        if (id != null)
            return id

        val lit = parseExpressionIntegralLiteral(t)
        if (lit != null)
            return lit

        return null
    }

    private fun parseExpressionFunctionCallStar(t: SQLTokenizer): IExpression?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()
            val tok = t.nextToken() // TODO: проверить что токен одидаемый
            if (t.hasMoreTokens() && t.nextToken() == SQLTokens.SYM_BRACE_OPEN.toString())
            {
                SymParser(SQLTokens.SYM_STAR).parseExpected(t)
                SymParser(SQLTokens.SYM_BRACE_CLOSE).parseExpected(t)
                return ExpressionFunctionCallStar(tok)
            }

            t.restorePosition(pos)
        }
        return null
    }

    private fun parseExpressionFunctionCall(t: SQLTokenizer): IExpression?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()
            val tok = t.nextToken() // TODO: проверить что токен одидаемый
            if (t.hasMoreTokens() && t.nextToken() == SQLTokens.SYM_BRACE_OPEN.toString())
            {
                if (SymParser(SQLTokens.SYM_BRACE_CLOSE).parse(t))
                    return ExpressionFunctionCall(tok)

                val args = mutableListOf<IExpression>()
                args.add(ExpressionParser().parseExpected(t))

                while (SymParser(SQLTokens.SYM_COMMA).parse(t))
                    args.add(ExpressionParser().parseExpected(t))

                return ExpressionFunctionCall(tok, args)
            }

            t.restorePosition(pos)
        }
        return null
    }

    private fun parseExpressionIdentifier(t: SQLTokenizer): IExpression?
    {
        if (t.hasMoreTokens())
        {
            val tok = t.nextToken() // TODO: проверить что токен одидаемый
            return ExpressionIdentifier(tok)
        }
        return null
    }

    private fun parseExpressionIntegralLiteral(t: SQLTokenizer): IExpression?
    {
        if (t.hasMoreTokens())
        {
            val tok = t.nextToken() // TODO: проверить что токен одидаемый
            return ExpressionIntegralLiteral(tok.toInt())
        }
        return null
    }
}

class ExpressionMulDivParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionBaseParser().parse(t)
        if (first == null)
            return null

        var expr = ExpressionBinary(first)
        while (SymParser(SQLTokens.SYM_MUL).parse(t) || SymParser(SQLTokens.SYM_DIV).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionBaseParser().parseExpected(t)))
        return expr
    }
}

class ExpressionPlusMinusParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionMulDivParser().parse(t)
        if (first == null)
            return null

        var expr = ExpressionBinary(first)
        while (SymParser(SQLTokens.SYM_PLUS).parse(t) || SymParser(SQLTokens.SYM_MINUS).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionMulDivParser().parseExpected(t)))
        return expr
    }
}

class ExpressionEqualsParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionPlusMinusParser().parse(t)
        if (first == null)
            return null

        var expr = ExpressionBinary(first)
        while (SymParser(SQLTokens.SYM_LESS).parse(t) || SymParser(SQLTokens.SYM_GREATER).parse(t) ||
            KeywordParser(SQLTokens.LESS_EQUAL).parse(t) || KeywordParser(SQLTokens.GREATER_EQUAL).parse(t))
        {
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionPlusMinusParser().parseExpected(t)))
        }
        return expr
    }
}

class ExpressionCompareParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionEqualsParser().parse(t)
        if (first == null)
            return null

        var expr = ExpressionBinary(first)
        while (SymParser(SQLTokens.SYM_ASSIGN).parse(t) || KeywordParser(SQLTokens.EQUAL).parse(t)
            || KeywordParser(SQLTokens.NOT_EQUAL).parse(t))
        {
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionEqualsParser().parseExpected(t)))
        }
        return expr
    }
}

class ExpressionAndParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionCompareParser().parse(t)
        if (first == null)
            return null

        var expr = ExpressionBinary(first)
        while (KeywordParser(SQLTokens.AND).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionCompareParser().parseExpected(t)))
        return expr
    }
}

class ExpressionParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionAndParser().parse(t)
        if (first == null)
            return null

        var expr = ExpressionBinary(first)
        while (KeywordParser(SQLTokens.OR).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionAndParser().parseExpected(t)))
        return expr
    }
}
