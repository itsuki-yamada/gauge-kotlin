/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package sample

import com.thoughtworks.gauge.Gauge
import com.thoughtworks.gauge.Step

class ClassSteps {
    @Step("Greet <name> from a regular class")
    fun greet(name: String) {
        Gauge.writeMessage("Hello, $name (regular class)")
    }
}
