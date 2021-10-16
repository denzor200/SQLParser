

class SQLTokenizer(str: String) {
    // TODO: убрать заглушку
    val TOKENS = listOf("SELECT", "*", "FROM", "book")

    val e_str: String
    var e_pos: Int
    init {
        e_str = str
        e_pos = 0
    }

    fun getPosition(): Int
    {
        return e_pos
    }

    fun restorePosition(pos: Int)
    {
        e_pos = pos
    }

    fun hasMoreTokens(): Boolean
    {
        return (e_pos < TOKENS.size)
    }
    fun nextToken(): String
    {
        val tok = TOKENS[e_pos]
        e_pos++
        return tok
    }
}