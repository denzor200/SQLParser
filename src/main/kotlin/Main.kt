
internal object SQLParserKt {
    @JvmStatic
    fun main(args: Array<String>) {
        while (true)
        {
            print("SQLParser> ")
            val line = readLine()
            if (line==null)
                break
            else
            {
                // TODO: implement this
                println("Parsed: $line")
            }
        }
    }
}