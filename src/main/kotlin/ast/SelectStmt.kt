package ast

import kotlinx.serialization.Serializable
import BaseParser
import SQLTokenizer
import KeywordParser
import SymParser

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

    private fun parseColumns(stmt: SelectStmt, t: SQLTokenizer): Boolean
    {
        if (!KeywordParser(SQLTokens.Keywords.SELECT).parse(t))
            return false
        stmt.columns.add(ColumnParser().parseExpected(t))

        while (SymParser(SQLTokens.Symbolized.SYM_COMMA).parse(t))
            stmt.columns.add(ColumnParser().parseExpected(t))

        return true
    }

    private fun parseFrom(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.FROM).parse(t))
            return
        stmt.from = FromParser().parseExpected(t)
    }

    private fun parseWhereClause(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.WHERE).parse(t))
            return
        stmt.whereClause = ExpressionParser().parseExpected(t)
    }

    private fun parseGroupByColumns(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.GROUP).parse(t))
            return

        KeywordParser(SQLTokens.Keywords.BY).parseExpected(t)
        stmt.groupByColumns.add(ExpressionParser().parseExpected(t))

        while (SymParser(SQLTokens.Symbolized.SYM_COMMA).parse(t))
            stmt.groupByColumns.add(ExpressionParser().parseExpected(t))

        parseHaving(stmt, t)
    }

    private fun parseHaving(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.HAVING).parse(t))
            return

        stmt.having = ExpressionParser().parseExpected(t)
    }

    private fun parseSortColumns(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.ORDER).parse(t))
            return

        KeywordParser(SQLTokens.Keywords.BY).parseExpected(t)
        stmt.sortColumns.add(ExpressionParser().parseExpected(t))

        while (SymParser(SQLTokens.Symbolized.SYM_COMMA).parse(t))
            stmt.sortColumns.add(ExpressionParser().parseExpected(t))
    }

    private fun parseLimit(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.LIMIT).parse(t))
            return

        stmt.limit = ExpressionParser().parseExpected(t)
        parseOffset(stmt, t)
    }

    private fun parseOffset(stmt: SelectStmt, t: SQLTokenizer)
    {
        if (!KeywordParser(SQLTokens.Keywords.OFFSET).parse(t))
            return

        stmt.offset = ExpressionParser().parseExpected(t)
    }

}


