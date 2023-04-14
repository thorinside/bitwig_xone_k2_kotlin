package com.nosuchdevice

import com.bitwig.extension.api.PlatformType
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList
import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.ControllerExtensionDefinition
import com.bitwig.extension.controller.api.ControllerHost
import java.util.*

class XoneK2ExtensionDefinition : ControllerExtensionDefinition() {
    override fun getName(): String = "XoneK2"

    override fun getAuthor(): String = "thorinside"

    override fun getVersion(): String = "0.0.1"

    override fun getId(): UUID = UUID.fromString("bb39163a-5b61-4548-908d-f4fed5b39786")

    override fun getRequiredAPIVersion(): Int = 18

    override fun getHardwareVendor(): String = "Allen & Heath"

    override fun getHardwareModel(): String = "XONE:K2"

    override fun getNumMidiInPorts(): Int = 1

    override fun getNumMidiOutPorts(): Int = 1

    override fun shouldFailOnDeprecatedUse(): Boolean {
        return true
    }

    override fun listAutoDetectionMidiPortNames(list: AutoDetectionMidiPortNamesList, platformType: PlatformType) {
        when (platformType) {
            PlatformType.WINDOWS -> {
                // TODO: Set the correct names of the ports for auto detection on Windows platform here
                // and uncomment this when port names are correct.
                // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
            }

            PlatformType.MAC -> {
                list.add(arrayOf("XONE:K2"), arrayOf("XONE:K2"))
            }

            PlatformType.LINUX -> {
                // TODO: Set the correct names of the ports for auto detection on Windows platform here
                // and uncomment this when port names are correct.
                // list.add(new String[]{"Input Port 0"}, new String[]{"Output Port 0"});
            }
        }
    }

    override fun createInstance(host: ControllerHost): ControllerExtension {
        return XoneK2Extension(this, host)
    }
}