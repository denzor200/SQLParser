import kotlin.reflect.KVisibility

object SQLAlpha
{
    const val ALPHA_SYM = ",()><=+-*/;"
    const val ALPHA_IDENT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_."
    // обратите внимание - точка тоже может входить в пользовательский идентификатор, это не символ, сделано для простоты реализации
    const val ALPHA_INTEGRAL = "0123456789"
}

object SQLTokens  {
    object Keywords {
        const val SELECT = "SELECT"
        const val FROM = "FROM"
        const val WHERE = "WHERE"
        const val GROUP = "GROUP"
        const val BY = "BY"
        const val AS = "AS"
        const val ON = "ON"
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
    }
    object Symbolized {
        /// Здесь принципиально соблюдать порядок объявления констант
        /// Наибольшие по размеру токены идут первыми
        const val LESS_EQUAL = "<="
        const val NOT_EQUAL = "!="
        const val GREATER_EQUAL = ">="
        const val EQUAL = "=="


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
        const val SYM_SEMICOLON = ';'
    }

    /**
     * см. алфавит токена в значении константы ALPHA_SYM
     * не всегда это строка размера 1, например токены "<=" ">=" и тд тоже входят в эту категорию
     */
    fun isSymbol(str: String): Boolean
    {
        for (it in Symbolized.javaClass.declaredFields)
        {
            val kw = it.get(Symbolized).toString()
            if (it.name != "INSTANCE" && kw.toString() == str)
            {
                return true
            }
        }
        return false
    }

    /**
     * конечное множество возможных значений токена см. в таблице Keywords
     * обратите внимание все значения прописаны в UPPERCASE, не бывает Keyword в lowercase
     * соответственно, токенизатор всегда отдает Keyword UPPERCASE, даже если от пользователся был получен смешанный case
     */
    fun isKeyword(str: String): Boolean
    {
        for (it in Keywords.javaClass.declaredFields)
        {
            val kw = it.get(Keywords).toString()
            if (it.name != "INSTANCE" && kw.toString() == str)
            {
                return true
            }
        }
        return false
    }

    /**
     * см. алфавит токена в значении константы IDENT_SYM
     * в эту категорию НЕ входят токены, которые:
     *   - состоят только из цифр
     *   - начинаются с цифры
     *   - являются ключевыми словами
     */
    fun isIdentifier(str: String): Boolean
    {
        if (isKeyword(str) || isIntegerLiteral(str))
            return false

        if (str.isNotEmpty() && SQLAlpha.ALPHA_INTEGRAL.any { it == str[0] })
            return false

        for (ch in str)
        {
            if (!SQLAlpha.ALPHA_IDENT.any { it == ch })
            {
                return false
            }
        }
        return true
    }

    /**
     * см. алфавит токена в значении константы INTEGRAL_SYM
     */
    fun isIntegerLiteral(str: String): Boolean
    {
        for (ch in str)
        {
            if (!SQLAlpha.ALPHA_INTEGRAL.any { it == ch })
            {
                return false
            }
        }
        return true
    }
}

class SQLTokenizer(str: String) {
    val e_str: String
    var e_pos: Int
    var e_lastToken: String
    init {
        e_str = str
        e_pos = 0
        e_lastToken = ""
    }

    /**
     * Получить значение текущей позиции
     */
    fun getPosition(): Int
    {
        return e_pos
    }

    /**
     * Установить значение текущей позиции
     */
    fun restorePosition(pos: Int)
    {
        e_pos = pos
    }

    /**
     * Проверить, есть ли еще токены после текущей позиции, пробелы игнорировать
     */
    fun hasMoreTokens(): Boolean
    {
        while (character().isWhitespace())
            e_pos++
        return (e_pos < e_str.length)
    }

    /**
     * Распарсить токен с текущей позиции, вернуть распарсеный токен
     * Допускается вызывать эту функцию только если hasMoreTokens вернул true
     */
    fun nextToken(): String
    {
        if (e_pos >= e_str.length)
        {
            throw EoiError()
        }

        var output: String = ""

        val isIdent = character() in SQLAlpha.ALPHA_IDENT
        val isSymbol = character() in SQLAlpha.ALPHA_SYM

        if (isIdent)
        {
            while (character() in SQLAlpha.ALPHA_IDENT)
                output += e_str[e_pos++]

            // поддерживаем UPPERCASE если это ключевое слово
            val uppOut = output.uppercase()
            if (SQLTokens.isKeyword(uppOut))
                output = uppOut

            // Не допускаем чтобы идентификаторы начинались с цифры
            if (!SQLTokens.isKeyword(output) && !SQLTokens.isIdentifier(output) && !SQLTokens.isIntegerLiteral(output))
            {
                throw SyntaxError(output)
            }
        }
        else if (isSymbol)
        {
            var matched = false

            /// Подбираем подходящий символьный токен полным перебором
            for (it in SQLTokens.Symbolized.javaClass.declaredFields)
            {
                val kw = it.get(SQLTokens.Symbolized).toString()
                if (it.name != "INSTANCE" && matchSymbolized(kw))
                {
                    output = kw
                    matched = true
                    break
                }
            }

            if (!matched)
            {
                throw SyntaxError(character())
            }
        }
        else
        {
            throw SyntaxError(character())
        }

        //println("TOKEN: $output")

        e_lastToken = output
        return output
    }

    /**
     * Еще раз получить токен, полученный последним вызовом nextToken
     * Поведение не определено, если nextToken не вызывался ранее, или был вызван restorePosition
     */
    fun lastToken(): String
    {
        return e_lastToken
    }

    /**
     * Получить символ(в том числе и пробельный символ) в текущей позиции
     * Спецсимвол 0 зарезервирован как EOI
     */
    fun character(): Char
    {
        if (e_pos < e_str.length)
            return e_str[e_pos]
        else
            return Char(0)
    }

    /**
     * Отвечает на вопрос "можем ли мы распарсить этот символьный токен с текущей позиции?"
     * Если функция вернула true, значит токен уже распарсен
     */
    private fun matchSymbolized(sym: String): Boolean
    {
        val pos = getPosition()
        for (ch in sym)
        {
            if (ch != character())
            {
                restorePosition(pos)
                return false
            }
            e_pos++
        }
        return true
    }

}