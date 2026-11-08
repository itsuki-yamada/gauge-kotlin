/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package sample

import com.thoughtworks.gauge.Gauge
import com.thoughtworks.gauge.Step

// The JVM method gets an extra Continuation parameter.
class SuspendSteps {
    @Step("Run a suspend function step")
    suspend fun suspendStep() {
        Gauge.writeMessage("suspend function")
    }
}
