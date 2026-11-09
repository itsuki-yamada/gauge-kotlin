/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package sample

import com.thoughtworks.gauge.Gauge
import com.thoughtworks.gauge.Step

// A singleton: the JVM class has a private constructor and a static INSTANCE field.
object ObjectSteps {
    @Step("Run a step in an object")
    fun objectStep() {
        Gauge.writeMessage("object")
    }
}
