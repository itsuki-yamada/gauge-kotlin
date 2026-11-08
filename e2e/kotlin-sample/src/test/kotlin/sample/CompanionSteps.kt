/*----------------------------------------------------------------
 *  Copyright (c) Itsuki Yamada
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE.txt in the project root for license information.
 *----------------------------------------------------------------*/
package sample

import com.thoughtworks.gauge.Gauge
import com.thoughtworks.gauge.Step

// Without @JvmStatic the method lives only in the nested class CompanionSteps$Companion.
class CompanionSteps {
    companion object {
        @Step("Run a step in a companion object")
        fun companionStep() {
            Gauge.writeMessage("companion object")
        }
    }
}

// With @JvmStatic a static method is also generated in the outer class.
class JvmStaticCompanionSteps {
    companion object {
        @JvmStatic
        @Step("Run a JvmStatic step in a companion object")
        fun jvmStaticCompanionStep() {
            Gauge.writeMessage("companion object with JvmStatic")
        }
    }
}
