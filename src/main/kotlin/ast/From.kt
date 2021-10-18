package ast

import BaseParser
import kotlinx.serialization.Serializable
import SQLTokenizer
import SymParser

@Serializable sealed class IFrom {
}

@Serializable class FromJoinClause(var tableOrSubquery: ITableOrSubquery
                                 , var joinOp: JoinOperator
                                 , var joinTableOrSubquery: ITableOrSubquery
                                 , var joinConstraint: IExpression) : IFrom()

@Serializable data class FromTableOrSubqueryList(val tableOrSubqueryList: MutableList<ITableOrSubquery> = mutableListOf()) : IFrom()

class FromParser : BaseParser<IFrom>()
{
    override fun parse(t: SQLTokenizer): IFrom?
    {
        if (t.hasMoreTokens())
        {
            val pos = t.getPosition()

            // Здесь порядок парсинга принципиален - сначала всегда пробуем распарсить JoinClause
            // Иначе JoinClause будет ошибочно воспринят как TableOrSubqueryList
            val jc = parseFromJoinClause(t)
            if (jc != null)
                return jc
            t.restorePosition(pos)

            val tosl = parseFromTableOrSubqueryList(t)
            if (tosl != null)
                return tosl
            t.restorePosition(pos)
        }
        return null
    }

    private fun parseFromTableOrSubqueryList(t: SQLTokenizer) : IFrom?
    {
        val tos = TableOrSubqueryParser().parse(t) ?: return null

        val tosl = FromTableOrSubqueryList()
        tosl.tableOrSubqueryList.add(tos)
        while (SymParser(SQLTokens.Symbolized.SYM_COMMA).parse(t))
            tosl.tableOrSubqueryList.add(TableOrSubqueryParser().parseExpected(t))

        return tosl;
    }

    private fun parseFromJoinClause(t: SQLTokenizer) : IFrom?
    {
        val pos = t.getPosition()
        val tos = TableOrSubqueryParser().parse(t) ?: return null

        val joinOp = JoinOperatorParser().parse(t);
        if (joinOp == null)
        {
            t.restorePosition(pos)
            return null
        }

        val joinTableOrSubquery = TableOrSubqueryParser().parseExpected(t)
        val joinConstraint = JoinConstraintParser().parseExpected(t)

        return FromJoinClause(tos, joinOp, joinTableOrSubquery, joinConstraint)
    }
}

