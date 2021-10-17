import interfaces.IFrom

abstract class BaseParser<T> {
    abstract fun parse(t: SQLTokenizer): T?

    fun parseExpected(t: SQLTokenizer): T
    {
        val obj = parse(t)
        if (obj==null)
        {
            // TODO: throw expectation failure
        }
        return obj!!
    }
}