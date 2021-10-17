package interfaces

// TODO: rename interfaces package

import kotlinx.serialization.Serializable
import SQLTokenizer
import BaseParser

@Serializable sealed class IColumn {
}

@Serializable data class Column(var expr: IExpression, var alias: String? = null) : IColumn()
@Serializable class ColumnStar() : IColumn()


class ColumnParser : BaseParser<IColumn>()
{
    override fun parse(t: SQLTokenizer): IColumn?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()
            val star = parseColumnStar(t)
            if (star != null)
                return star
            t.restorePosition(pos)

            val column = parseColumn(t)
            if (column != null)
                return column
            t.restorePosition(pos)
        }
        return null
    }

    private fun parseColumnStar(t: SQLTokenizer): IColumn?
    {
        val tok = t.nextToken()
        if (tok == SQLTokens.SYM_STAR.toString())
            return ColumnStar()
        return null
    }

    private fun parseColumn(t: SQLTokenizer): IColumn?
    {
        val expr = ExpressionParser().parse(t)
        if (expr != null)
            return Column(expr)
        return null
    }
}



