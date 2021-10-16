package interfaces

import kotlinx.serialization.Serializable
import SQLTokenizer

@Serializable sealed class IExpression {
}

class ExpressionParser
{
    fun parse(t: SQLTokenizer): IExpression?
    {
        // TODO: implement this
        return null
    }
}
