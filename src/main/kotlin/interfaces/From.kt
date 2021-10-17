package interfaces

import BaseParser
import kotlinx.serialization.Serializable
import SQLTokenizer

// TODO: вынести
@Serializable sealed class ITableOrSubquery {}

@Serializable data class Subquery(val stmt: ISelectStmt, val alias: String? = null) : ITableOrSubquery()

@Serializable data class TableName(val tableName: String, val alias: String?=null) : ITableOrSubquery()
/////////////////////////////////////////////

// TODO: вынести
@Serializable enum class JoinOperator
{
    INNER, LEFT, RIGHT, FULL
}


////////////////////////////////////////////////

@Serializable sealed class IFrom {
}

@Serializable class FromJoinClause(val tableOrSubquery: ITableOrSubquery
                                 , val joinOp: JoinOperator
                                 , val joinTableOrSubquery: ITableOrSubquery
                                 , val joinConstraint: IExpression) : IFrom()

@Serializable data class FromTableOrSubqueryList(val tableOrSubqueryList: List<ITableOrSubquery>) : IFrom()

class FromParser : BaseParser<IFrom>()
{
    override fun parse(t: SQLTokenizer): IFrom?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()

            // Здесь порядок принципиален. Сначала всегда пробуем FromTableOrSubqueryList
            val tosl = parseFromTableOrSubqueryList(t)
            if (tosl != null)
                return tosl
            t.restorePosition(pos)

            val jc = parseFromJoinClause(t)
            if (jc != null)
                return jc
            t.restorePosition(pos)
        }
        return null
    }

    private fun parseFromTableOrSubqueryList(t: SQLTokenizer) : IFrom?
    {
        val tok = t.nextToken()
        // TODO: проверить токен
        // TODO: реализовать парсинг списка из нескольких значений
        // TODO: в этом списке может быть не только TableName
        return FromTableOrSubqueryList(listOf(TableName(tok)))
    }

    private fun parseFromJoinClause(t: SQLTokenizer) : IFrom?
    {
        // TODO: implement this
        return null
    }
}

