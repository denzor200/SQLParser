package interfaces

import kotlinx.serialization.Serializable
import SQLTokenizer

// TODO: вынести
@Serializable sealed class ITableOrSubquery {}

// TODO: implement this
@Serializable class Subquery() : ITableOrSubquery()

// TODO: optional scheme name
// TODO: optional alias
@Serializable data class TableName(val tableName: String) : ITableOrSubquery()



@Serializable sealed class IFrom {
}

// TODO: implement this
@Serializable class FromJoinClause() : IFrom()

@Serializable data class FromTableOrSubqueryList(val tableOrSubqueryList: List<ITableOrSubquery>) : IFrom()

// TODO: нужен базовый класс парсера
class FromParser
{
    fun parse(t: SQLTokenizer): IFrom?
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

    fun parseExpected(t: SQLTokenizer): IFrom
    {
        val from = parse(t)
        if (from==null)
        {
            // TODO: throw expectation failure
        }
        return from!!
    }
}

