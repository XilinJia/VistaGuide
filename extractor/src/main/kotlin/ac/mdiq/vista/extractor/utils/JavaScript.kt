package ac.mdiq.vista.extractor.utils

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function


object JavaScript {

    fun compileOrThrow(function: String?) {
        Context.enter().use { context ->
            context.optimizationLevel = -1
            // If it doesn't compile it throws an exception here
            context.compileString(function, null, 1, null)
        }
    }

    fun run(function: String?, functionName: String?, vararg parameters: String?): String {
        Context.enter().use { context ->
            context.optimizationLevel = -1
            val scope = context.initSafeStandardObjects()

            context.evaluateString(scope, function, functionName, 1, null)
            val jsFunction = scope.get(functionName, scope) as Function
            val result = jsFunction.call(context, scope, scope, parameters)
            return result.toString()
        }
    }
}
