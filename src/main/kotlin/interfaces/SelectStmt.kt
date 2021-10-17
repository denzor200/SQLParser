package interfaces

import kotlinx.serialization.Serializable
import BaseParser
import SQLTokenizer

@Serializable
sealed class ISelectStmt {
}

@Serializable data class SelectStmt(var columns: MutableList<IColumn> = mutableListOf(),
                                    var from: IFrom? = null,
                                    var whereClause: IExpression? = null,
                                    var groupByColumns: MutableList<IExpression> = mutableListOf(),
                                    var having: IExpression? = null,
                                    var sortColumns: MutableList<IExpression> = mutableListOf(),
                                    var limit: IExpression? = null,
                                    var offset: IExpression? = null) : ISelectStmt()

class SelectStmtParser : BaseParser<ISelectStmt>()
{
    override fun parse(t: SQLTokenizer): ISelectStmt?
    {
        var stmt = SelectStmt()
        if (!parseColumns(stmt, t))
            return null
        parseFrom(stmt, t)
        parseWhereClause(stmt, t)
        parseGroupByColumns(stmt, t)
        parseSortColumns(stmt, t)
        parseLimit(stmt, t)
        return stmt
    }


    // TODO: вынести BEGIN
    private fun parseKeyword(kw: String, t: SQLTokenizer): Boolean
    {
        val pos = t.getPosition()
        if (t.hasMoreTokens() && t.nextToken() == kw)
            return true
        t.restorePosition(pos)
        return false
    }

    private fun parseSym(sym: Char, t: SQLTokenizer): Boolean
    {
        val pos = t.getPosition()
        if (t.hasMoreTokens() && t.nextToken() == sym.toString())
            return true
        t.restorePosition(pos)
        return false
    }

    private fun parseExpectedKeyword(kw: String, t: SQLTokenizer)
    {
        if (!parseKeyword(kw, t))
        {
            // TODO: throw expectation failure
        }
    }
    // TODO: вынести END


    private fun parseColumns(stmt: SelectStmt, t: SQLTokenizer): Boolean
    {
        if (!parseKeyword("SELECT", t))
            return false
        stmt.columns.add(ColumnParser().parseExpected(t))

        while (parseSym(',', t))
            stmt.columns.add(ColumnParser().parseExpected(t))

        return true
    }

    private fun parseFrom(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!parseKeyword("FROM", t))
            return
        stmt.from = FromParser().parseExpected(t)
    }

    private fun parseWhereClause(stmt: SelectStmt, t: SQLTokenizer)
    {
        // TODO: implement this
    }

    private fun parseGroupByColumns(stmt: SelectStmt, t: SQLTokenizer)
    {
        // TODO: implement this
        // TODO: parse having
    }

    private fun parseSortColumns(stmt: SelectStmt, t: SQLTokenizer)
    {
        // TODO: implement this
    }

    private fun parseLimit(stmt: SelectStmt, t: SQLTokenizer)
    {
        // TODO: implement this
        // TODO: parse offset
    }
}


