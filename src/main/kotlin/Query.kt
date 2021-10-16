import interfaces.*
import kotlinx.serialization.Serializable

// TODO: it's must be a recursive class
@Serializable class Query {
    var columns: MutableList<IColumn> = mutableListOf()
    var from: IFrom? = null
    var whereClause: IExpression? = null
    var groupByColumns: MutableList<IExpression> = mutableListOf()
    var sortColumns: MutableList<IExpression> = mutableListOf()
    var limit: IExpression? = null
    var offset: IExpression? = null

    fun parse(str: String)
    {
        var t = SQLTokenizer(str)
        parseColumns(t)
        parseFrom(t)
        parseWhereClause(t)
        parseGroupByColumns(t)
        parseSortColumns(t)
        parseLimit(t)
        parseOffset(t)
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

    private fun parseColumns(t: SQLTokenizer)
    {
        parseExpectedKeyword("SELECT", t)
        columns.add(ColumnParser().parseExpected(t))

        while (parseSym(',', t))
            columns.add(ColumnParser().parseExpected(t))
    }

    private fun parseFrom(t: SQLTokenizer)
    {
        if (!parseKeyword("FROM", t))
            return
        from = FromParser().parseExpected(t)
    }

    private fun parseWhereClause(t: SQLTokenizer)
    {
        // TODO: implement this
    }

    private fun parseGroupByColumns(t: SQLTokenizer)
    {
        // TODO: implement this
    }

    private fun parseSortColumns(t: SQLTokenizer)
    {
        // TODO: implement this
    }

    private fun parseLimit(t: SQLTokenizer)
    {
        // TODO: implement this
    }

    private fun parseOffset(t: SQLTokenizer)
    {
        // TODO: implement this
    }
};
