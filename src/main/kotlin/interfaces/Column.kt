package interfaces

// TODO: rename interfaces package

import kotlinx.serialization.Serializable
import SQLTokenizer
import java.beans.Expression

@Serializable sealed class IColumn {
}

// TODO: add optional column alias
@Serializable data class Column(val expr: IExpression) : IColumn()
@Serializable class ColumnStar() : IColumn()


class ColumnParser
{
    fun parse(t: SQLTokenizer): IColumn?
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
        if (tok == "*")
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

    fun parseExpected(t: SQLTokenizer): IColumn
    {
        val from = parse(t)
        if (from==null)
        {
            // TODO: throw expectation failure
        }
        return from!!
    }
}



