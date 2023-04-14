package com.nosuchdevice.transport

import com.bitwig.extension.controller.api.HardwareSurface
import com.bitwig.extension.controller.api.MidiIn
import com.bitwig.extension.controller.api.Transport
import com.nosuchdevice.XoneK2Hardware
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_SETUP
import com.nosuchdevice.XoneK2Hardware.Companion.LIGHT_0
import com.nosuchdevice.XoneK2Hardware.Companion.LIGHT_1
import com.nosuchdevice.XoneK2Hardware.Companion.LIGHT_2
import com.nosuchdevice.XoneK2Hardware.Companion.OFF
import com.nosuchdevice.XoneK2Hardware.Companion.RED
import com.nosuchdevice.XoneK2Hardware.Companion.YELLOW
import com.nosuchdevice.track.LightState

class TransportHandler(
    val inPort: MidiIn,
    val transport: Transport,
    val hardwareSurface: HardwareSurface,
    val hardware: XoneK2Hardware
) {

    init {
        val playButton = hardwareSurface.createHardwareButton("PLAY_BUTTON")
        val playButtonLight = hardwareSurface.createOnOffHardwareLight("PLAY_BUTTON_LIGHT")

        playButton.setBackgroundLight(playButtonLight)
        playButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, BUTTON_SETUP))
        playButton.pressedAction().setBinding(transport.playAction())

        val light0 = hardwareSurface.createMultiStateHardwareLight("LIGHT_0")
        val light1 = hardwareSurface.createMultiStateHardwareLight("LIGHT_1")
        val light2 = hardwareSurface.createMultiStateHardwareLight("LIGHT_2")

        transport.isPlaying.markInterested()
        transport.isArrangerRecordEnabled.markInterested()
        transport.isFillModeActive.markInterested()

        light0.state().setValueSupplier {
            LightState(
                when {
                    transport.isPlaying.get() -> YELLOW
                    else -> OFF
                }
            )
        }

        light1.state().setValueSupplier {
            LightState(
                when {
                    transport.isArrangerRecordEnabled.get() -> RED
                    else -> OFF
                }
            )
        }

        light2.state().setValueSupplier {
            LightState(
                when {
                    transport.isFillModeActive.get() -> YELLOW
                    else -> OFF
                }
            )
        }

        transport.isPlaying.markInterested()
        playButtonLight.isOn.setValueSupplier {
            transport.isPlaying.get()
        }

        playButtonLight.isOn.onUpdateHardware {
            hardware.updateLED(BUTTON_SETUP, if (it) YELLOW else OFF)
        }

        light0.state().onUpdateHardware {
            hardware.updateLED(LIGHT_0, (it as LightState).color)
        }


        light1.state().onUpdateHardware {
            hardware.updateLED(LIGHT_1, (it as LightState).color)
        }

        light2.state().onUpdateHardware {
            hardware.updateLED(LIGHT_2, (it as LightState).color)
        }
    }
}