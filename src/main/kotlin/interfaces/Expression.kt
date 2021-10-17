package interfaces

import kotlinx.serialization.Serializable
import SQLTokenizer
import BaseParser

@Serializable sealed class IExpression {
}

@Serializable data class ExpressionIntegralLiteral(var value: Int) : IExpression()
@Serializable data class ExpressionIdentifier(var value: String) : IExpression()
@Serializable data class ExpressionFunctionCall(var value: String, var args: List<IExpression>) : IExpression()
@Serializable data class ExpressionFunctionCallStar(var value: String) : IExpression()
@Serializable data class ExpressionBinary(var left: IExpression, var tok: String, var right: IExpression) : IExpression()

class ExpressionParser : BaseParser<IExpression>()
{
    override fun parse(t: SQLTokenizer): IExpression?
    {
        // TODO: implement this
        return null
    }
}
