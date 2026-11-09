/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package sample

import com.thoughtworks.gauge.Gauge
import com.thoughtworks.gauge.Step

// The JVM method takes two parameters, while the step text has one.
class DefaultArgumentSteps {
    @Step("Run a step with a default argument <first>")
    fun withDefault(first: String, second: String = "default") {
        Gauge.writeMessage("first=$first, second=$second")
    }
}
