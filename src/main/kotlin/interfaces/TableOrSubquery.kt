package interfaces

import kotlinx.serialization.Serializable
import BaseParser
import SQLTokenizer
import SymParser
import KeywordParser
import java.security.Key

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
        if (!SymParser(SQLTokens.SYM_BRACE_OPEN).parse(t))
            return null

        val sq = Subquery(SelectStmtParser().parseExpected(t))
        SymParser(SQLTokens.SYM_BRACE_CLOSE).parseExpected(t)

        sq.alias = parseOptionalAlias(t)

        return sq
    }

    private fun parseTableName(t: SQLTokenizer): ITableOrSubquery?
    {
        if (t.hasMoreTokens())
        {
            // TODO: проверить что токен ожидаем
            val tb = TableName(t.nextToken())
            tb.alias = parseOptionalAlias(t)
            return tb
        }
        return null
    }

    private fun parseOptionalAlias(t: SQLTokenizer): String?
    {
        if (!KeywordParser(SQLTokens.AS).parse(t))
            return null

        // TODO: проверить что токен существует и что он ожидаем
        return t.nextToken()
    }
}

