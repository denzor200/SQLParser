package ast

import kotlinx.serialization.Serializable
import BaseParser
import SQLTokenizer
import SymParser
import KeywordParser
import IdentifierParser

@Serializable
sealed class ITableOrSubquery {}

@Serializable
data class Subquery(var stmt: ISelectStmt, var alias: String? = null) : ITableOrSubquery()

@Serializable
data class TableName(var tableName: String, var alias: String?=null) : ITableOrSubquery()

class TableOrSubqueryParser : BaseParser<ITableOrSubquery>()
{
    override fun parse(t: SQLTokenizer): ITableOrSubquery?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()

            val sq = parseSubquery(t)
            if (sq != null)
                return sq
            t.restorePosition(pos)

            val tn = parseTableName(t)
            if (tn != null)
                return tn
            t.restorePosition(pos)
        }
        return null
    }

    private fun parseSubquery(t: SQLTokenizer): ITableOrSubquery?
    {
        if (!SymParser(SQLTokens.Symbolized.SYM_BRACE_OPEN).parse(t))
            return null

        val sq = Subquery(SelectStmtParser().parseExpected(t))
        SymParser(SQLTokens.Symbolized.SYM_BRACE_CLOSE).parseExpected(t)

        sq.alias = parseOptionalAlias(t)

        return sq
    }

    private fun parseTableName(t: SQLTokenizer): ITableOrSubquery?
    {
        //println("parseTableName")
        val tok = IdentifierParser().parse(t)
        if (tok != null)
        {
            val tb = TableName(tok)
            tb.alias = parseOptionalAlias(t)
            return tb
        }
        return null
    }

    private fun parseOptionalAlias(t: SQLTokenizer): String?
    {
        if (KeywordParser(SQLTokens.Keywords.AS).parse(t))
        {
            return IdentifierParser().parseExpected(t)
        }

        return IdentifierParser().parse(t)
    }
}

