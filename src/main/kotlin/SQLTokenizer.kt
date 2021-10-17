
object SQLTokens {
    const val SELECT = "SELECT"
    const val FROM = "FROM"
    const val WHERE = "WHERE"
    const val GROUP = "GROUP"
    const val BY = "BY"
    const val AS = "AS"
    const val HAVING = "HAVING"
    const val ORDER = "ORDER"
    const val LIMIT = "LIMIT"
    const val OFFSET = "OFFSET"
    const val INNER = "INNER"
    const val LEFT = "LEFT"
    const val RIGHT = "RIGHT"
    const val FULL = "FULL"
    const val JOIN = "JOIN"
    const val AND = "AND"
    const val OR = "OR"
    const val SYM_STAR = '*'
    const val SYM_COMMA = ','
    const val SYM_BRACE_OPEN = '('
    const val SYM_BRACE_CLOSE = ')'
    const val SYM_MUL = '*'
    const val SYM_DIV = '/'
    const val SYM_PLUS = '+'
    const val SYM_MINUS = '-'
    const val SYM_LESS = '<'
    const val SYM_GREATER = '>'
    const val SYM_ASSIGN = '='

    const val LESS_EQUAL = "<="
    const val GREATER_EQUAL = ">="
    const val EQUAL = "=="
    const val NOT_EQUAL = "!="

}

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

    fun lastToken(): String
    {
        // TODO: implement this
        return ""
    }
}