/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package sample

import com.thoughtworks.gauge.Gauge
import com.thoughtworks.gauge.Step

// Compiled to a static method of the file facade class TopLevelStepsKt.
@Step("Run a top-level function step")
fun topLevelStep() {
    Gauge.writeMessage("top-level function")
}
