package interfaces

import kotlinx.serialization.Serializable
import SQLTokenizer
import BaseParser
import SymParser
import KeywordParser
import IntegerLiteralParser
import IdentifierParser

@Serializable sealed class IExpression {
}

@Serializable data class ExpressionOperation(var tok: String, var operand: IExpression)

@Serializable data class ExpressionIntegralLiteral(var value: Int) : IExpression()
@Serializable data class ExpressionIdentifier(var value: String) : IExpression()
@Serializable data class ExpressionFunctionCall(var value: String, var args: MutableList<IExpression> = mutableListOf()) : IExpression()
@Serializable data class ExpressionFunctionCallStar(var value: String) : IExpression()
@Serializable data class ExpressionProgram(var first: IExpression, var rest: MutableList<ExpressionOperation> = mutableListOf()) : IExpression()



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

        if (SymParser(SQLTokens.Symbolized.SYM_BRACE_OPEN).parse(t))
        {
            val expr = ExpressionParser().parseExpected(t)
            SymParser(SQLTokens.Symbolized.SYM_BRACE_CLOSE).parseExpected(t)
            return expr
        }

        return null
    }

    private fun parseExpressionFunctionCallStar(t: SQLTokenizer): IExpression?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()
            val tok = IdentifierParser().parse(t)
            if (tok != null && t.hasMoreTokens() && t.nextToken() == SQLTokens.Symbolized.SYM_BRACE_OPEN.toString()
                && SymParser(SQLTokens.Symbolized.SYM_STAR).parse(t))
            {

                SymParser(SQLTokens.Symbolized.SYM_BRACE_CLOSE).parseExpected(t)
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
            val tok = IdentifierParser().parse(t)
            if (tok != null && t.hasMoreTokens() && t.nextToken() == SQLTokens.Symbolized.SYM_BRACE_OPEN.toString()
                && !SymParser(SQLTokens.Symbolized.SYM_STAR).parse(t))
            {
                if (SymParser(SQLTokens.Symbolized.SYM_BRACE_CLOSE).parse(t))
                    return ExpressionFunctionCall(tok)

                val args = mutableListOf<IExpression>()
                args.add(ExpressionParser().parseExpected(t))

                while (SymParser(SQLTokens.Symbolized.SYM_COMMA).parse(t))
                    args.add(ExpressionParser().parseExpected(t))

                SymParser(SQLTokens.Symbolized.SYM_BRACE_CLOSE).parseExpected(t)
                return ExpressionFunctionCall(tok, args)
            }

            t.restorePosition(pos)
        }
        return null
    }

    private fun parseExpressionIdentifier(t: SQLTokenizer): IExpression?
    {
        val tok = IdentifierParser().parse(t)
        if (tok != null)
            return ExpressionIdentifier(tok)
        return null
    }

    private fun parseExpressionIntegralLiteral(t: SQLTokenizer): IExpression?
    {
        val tok = IntegerLiteralParser().parse(t)
        if (tok != null)
            return ExpressionIntegralLiteral(tok.toInt())
        return null
    }
}

class ExpressionMulDivParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionBaseParser().parse(t) ?: return null

        val expr = ExpressionProgram(first)
        while (SymParser(SQLTokens.Symbolized.SYM_MUL).parse(t) || SymParser(SQLTokens.Symbolized.SYM_DIV).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionBaseParser().parseExpected(t)))
        return if (expr.rest.isEmpty()) first else expr
    }
}

class ExpressionPlusMinusParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionMulDivParser().parse(t) ?: return null

        val expr = ExpressionProgram(first)
        while (SymParser(SQLTokens.Symbolized.SYM_PLUS).parse(t) || SymParser(SQLTokens.Symbolized.SYM_MINUS).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionMulDivParser().parseExpected(t)))
        return if (expr.rest.isEmpty()) first else expr
    }
}

class ExpressionEqualsParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionPlusMinusParser().parse(t) ?: return null

        val expr = ExpressionProgram(first)
        while (SymParser(SQLTokens.Symbolized.SYM_LESS).parse(t) || SymParser(SQLTokens.Symbolized.SYM_GREATER).parse(t) ||
            KeywordParser(SQLTokens.Symbolized.LESS_EQUAL).parse(t) || KeywordParser(SQLTokens.Symbolized.GREATER_EQUAL).parse(t))
        {
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionPlusMinusParser().parseExpected(t)))
        }
        return if (expr.rest.isEmpty()) first else expr
    }
}

class ExpressionCompareParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionEqualsParser().parse(t) ?: return null

        val expr = ExpressionProgram(first)
        while (SymParser(SQLTokens.Symbolized.SYM_ASSIGN).parse(t) || KeywordParser(SQLTokens.Symbolized.EQUAL).parse(t)
            || KeywordParser(SQLTokens.Symbolized.NOT_EQUAL).parse(t))
        {
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionEqualsParser().parseExpected(t)))
        }
        return if (expr.rest.isEmpty()) first else expr
    }
}

class ExpressionAndParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionCompareParser().parse(t) ?: return null

        val expr = ExpressionProgram(first)
        while (KeywordParser(SQLTokens.Keywords.AND).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionCompareParser().parseExpected(t)))
        return if (expr.rest.isEmpty()) first else expr
    }
}

class ExpressionParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        val first = ExpressionAndParser().parse(t) ?: return null

        val expr = ExpressionProgram(first)
        while (KeywordParser(SQLTokens.Keywords.OR).parse(t))
            expr.rest.add(ExpressionOperation(t.lastToken(), ExpressionAndParser().parseExpected(t)))
        return if (expr.rest.isEmpty()) first else expr
    }
}
