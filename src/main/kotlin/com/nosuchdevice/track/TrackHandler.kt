package com.nosuchdevice.track

import com.bitwig.extension.api.Color
import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.controller.api.*
import com.nosuchdevice.XoneK2Hardware
import com.nosuchdevice.XoneK2Hardware.Companion.ABS_0
import com.nosuchdevice.XoneK2Hardware.Companion.BLINK_GREEN
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_1_0
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_1_1
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_1_2
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_1_3
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_2_0
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_2_1
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_2_2
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_2_3
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_3_0
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_3_1
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_3_2
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_3_3
import com.nosuchdevice.XoneK2Hardware.Companion.BUTTON_LAYER
import com.nosuchdevice.XoneK2Hardware.Companion.FADER_0
import com.nosuchdevice.XoneK2Hardware.Companion.FADER_1
import com.nosuchdevice.XoneK2Hardware.Companion.FADER_2
import com.nosuchdevice.XoneK2Hardware.Companion.FADER_3
import com.nosuchdevice.XoneK2Hardware.Companion.GREEN
import com.nosuchdevice.XoneK2Hardware.Companion.LIGHT_3
import com.nosuchdevice.XoneK2Hardware.Companion.OFF
import com.nosuchdevice.XoneK2Hardware.Companion.RED
import com.nosuchdevice.XoneK2Hardware.Companion.REL_0
import com.nosuchdevice.XoneK2Hardware.Companion.REL_0_CLICK
import com.nosuchdevice.XoneK2Hardware.Companion.REL_1
import com.nosuchdevice.XoneK2Hardware.Companion.REL_1_CLICK
import com.nosuchdevice.XoneK2Hardware.Companion.REL_2
import com.nosuchdevice.XoneK2Hardware.Companion.REL_2_CLICK
import com.nosuchdevice.XoneK2Hardware.Companion.REL_3
import com.nosuchdevice.XoneK2Hardware.Companion.REL_3_CLICK
import com.nosuchdevice.XoneK2Hardware.Companion.REL_4
import com.nosuchdevice.XoneK2Hardware.Companion.REL_4_BUTTON
import com.nosuchdevice.XoneK2Hardware.Companion.REL_5
import com.nosuchdevice.XoneK2Hardware.Companion.REL_5_BUTTON
import com.nosuchdevice.XoneK2Hardware.Companion.YELLOW
import java.util.*
import java.util.function.Supplier


class LightState(val color: Int) : InternalHardwareLightState() {
    override fun getVisualState(): HardwareLightVisualState {
        return when (color) {
            YELLOW -> HardwareLightVisualState.createForColor(Color.fromRGB(1.0, 1.0, 0.0))
            RED -> HardwareLightVisualState.createForColor(Color.fromRGB(1.0, 0.0, 0.0))
            GREEN -> HardwareLightVisualState.createForColor(Color.fromRGB(0.0, 1.0, 0.0))
            BLINK_GREEN -> HardwareLightVisualState.createBlinking(
                Color.fromRGB(0.0, 1.0, 0.0),
                Color.fromRGB(1.0, 1.0, 0.0),
                1.0,
                1.0
            )

            OFF -> HardwareLightVisualState.createForColor(Color.blackColor())
            else -> HardwareLightVisualState.createForColor(Color.nullColor())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LightState

        return color == other.color
    }

    override fun hashCode(): Int {
        return color
    }

}

class TrackHandler(
    private val inPort: MidiIn,
    private val trackBank: TrackBank,
    private val cursorTrack: CursorTrack,
    private val hardwareSurface: HardwareSurface,
    private val hardware: XoneK2Hardware,
    private val host: ControllerHost,
) {

    enum class NavigationMode {
        SCENE,
        DEVICE,
        TRACK,
    }

    private var currentNavigationMode: NavigationMode = NavigationMode.SCENE
    private var isShiftPressed: Boolean = false

    private val sceneBank = trackBank.sceneBank()

    private val cursorDevice = cursorTrack.createCursorDevice(
        "XONE_CURSOR_DEVICE",
        "Cursor Device",
        0,
        CursorDeviceFollowMode.FOLLOW_SELECTION
    )
    private val remoteControlBank = cursorDevice.createCursorRemoteControlsPage(12)

    private val rel4 = hardwareSurface.createRelativeHardwareKnob("REL_4")
    private val rel5 = hardwareSurface.createRelativeHardwareKnob("REL_5")

    private var verticalNavigation: HardwareBinding? = null
    private var horizontalNavigation: HardwareBinding? = null

    init {
        trackBank.setShouldShowClipLauncherFeedback(true)

        addNavigationKnobs()

        addVolumeFaders()
        addPanners()
        addArms()
        addSolos()
        addMutes()
        updateNavigation(currentNavigationMode)
        addNavigationModeButton()
        addWindowOpenToggle()
        addDeviceEnabledToggle()
        addShiftButton()
        addClipLaunching()
        addRemoteControlKnobs()

        trackBank.followCursorTrack(cursorTrack)

        cursorTrack.solo().markInterested()
        cursorTrack.mute().markInterested()

        cursorDevice.isEnabled.markInterested()
        cursorDevice.isWindowOpen.markInterested()

    }

    private fun addRemoteControlKnobs() {
        for (i in 0 until remoteControlBank.parameterCount) {
            remoteControlBank.getParameter(i).apply {
                markInterested()
                setIndication(true)
                val absoluteHardwareKnob = hardwareSurface.createAbsoluteHardwareKnob("KNOB_$i")
                absoluteHardwareKnob.setAdjustValueMatcher(inPort.createAbsoluteCCValueMatcher(0, ABS_0 + i))
                addBinding(absoluteHardwareKnob)
            }
        }
    }

    private fun addNavigationKnobs() {
        // Set up navigation knobs
        rel4.setAdjustValueMatcher(inPort.createRelative2sComplementCCValueMatcher(0, REL_4, 10))
        rel5.setAdjustValueMatcher(inPort.createRelative2sComplementCCValueMatcher(0, REL_5, 10))
    }

    private fun addClipLaunching() {
        for (i in 0 until trackBank.sizeOfBank) {
            val track = this.trackBank.getItemAt(i)

            for (j in 0 until track.clipLauncherSlotBank().sizeOfBank) {
                val clip = track.clipLauncherSlotBank().getItemAt(j)
                clip.isPlaybackQueued.markInterested()
                clip.isPlaying.markInterested()
                clip.isRecording.markInterested()
                clip.hasContent().markInterested()

                val playButton = hardwareSurface.createHardwareButton("PLAY_BUTTON_${j}_$i")
                val playButtonLight = hardwareSurface.createMultiStateHardwareLight("PLAY_BUTTON_LIGHT_${j}_$i")

                playButton.setBackgroundLight(playButtonLight)
                val buttonNote = hardware.getLEDFor(j, i)

                playButton.releasedAction().setActionMatcher(inPort.createNoteOffActionMatcher(0, buttonNote))
                playButton.releasedAction().setBinding(host.createAction(Runnable {
                    if (clip.isPlaying.get()) {
                        if (isShiftPressed) {
                            clip.launchReleaseAlt()
                        } else {
                            clip.launchRelease()
                        }
                    }
                }, Supplier { "Release playing" }))

                playButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, buttonNote))
                playButton.pressedAction().setBinding(host.createAction(Runnable {
                    if (clip.isPlaying.get() || clip.isRecording.get()) {
                        track.stop()
                    } else {
                        if (isShiftPressed) {
                            clip.launchAlt()
                        } else {
                            clip.launch()
                        }
                    }
                }, Supplier {
                    "Toggle playing"
                }))

                playButtonLight.state().setValueSupplier {
                    LightState(
                        when {
                            clip.isPlaybackQueued.get() -> BLINK_GREEN
                            clip.isPlaying.get() -> GREEN
                            clip.isRecording.get() -> RED
                            clip.hasContent().get() -> YELLOW
                            else -> OFF
                        }
                    )
                }

                playButtonLight.state().onUpdateHardware {
                    if (it.visualState.isBlinking) {
                        hardware.blinkLED(buttonNote)
                    } else {
                        hardware.cancelBlink(buttonNote)
                        hardware.updateLED(buttonNote, (it as LightState).color)
                    }
                }
            }

            var p = track.pan()
            p.markInterested()
            p.setIndication(true)

            p = track.volume()
            p.markInterested()
            p.setIndication(true)
        }
    }

    private fun addShiftButton() {
        val shiftButton = hardwareSurface.createHardwareButton("REL_4_BUTTON")
        val shiftButtonLight = hardwareSurface.createMultiStateHardwareLight("LIGHT_3")

        shiftButton.setBackgroundLight(shiftButtonLight)
        shiftButton.releasedAction().setActionMatcher(inPort.createNoteOffActionMatcher(0, REL_4_BUTTON))
        shiftButton.releasedAction().setBinding(host.createAction(Runnable {
            isShiftPressed = false
        }, Supplier { "Release Shift" }))
        shiftButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_4_BUTTON))
        shiftButton.pressedAction().setBinding(host.createAction(Runnable {
            isShiftPressed = true
        }, Supplier {
            "Shift"
        }))

        shiftButtonLight.state().setValueSupplier {
            LightState(
                when {
                    isShiftPressed -> GREEN
                    else -> OFF
                }
            )
        }
        shiftButtonLight.state().onUpdateHardware {
            if (it.visualState.isBlinking) {
                hardware.blinkLED(LIGHT_3)
            } else {
                hardware.cancelBlink(LIGHT_3)
                hardware.updateLED(LIGHT_3, (it as LightState).color)
            }
        }
    }

    private fun addNavigationModeButton() {
        val hardwareButton = hardwareSurface.createHardwareButton("LAYER")
        hardwareButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, BUTTON_LAYER))
        hardwareButton.pressedAction().setBinding(
            host.createAction(Runnable {
                updateNavigation(
                    when (currentNavigationMode) {
                        NavigationMode.SCENE -> NavigationMode.TRACK
                        NavigationMode.TRACK -> NavigationMode.DEVICE
                        NavigationMode.DEVICE -> NavigationMode.SCENE
                    }
                )
            }, Supplier { "Change Navigation Mode" })
        )
    }

    private fun addWindowOpenToggle() {
        val hardwareButton = hardwareSurface.createHardwareButton("OPEN_WINDOW")
        hardwareButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_5_BUTTON))
        hardwareButton.pressedAction().setBinding(
            host.createAction(Runnable {
                if (currentNavigationMode == NavigationMode.DEVICE) {
                    cursorDevice.isWindowOpen.toggle()
                }
            }, Supplier { "Toggle Device Window" })
        )
    }

    private fun addDeviceEnabledToggle() {
        val hardwareButton = hardwareSurface.createHardwareButton("ENABLE_DEVICE")
        hardwareButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_4_BUTTON))
        hardwareButton.pressedAction().setBinding(
            host.createAction(Runnable {
                if (currentNavigationMode == NavigationMode.DEVICE) {
                    cursorDevice.isEnabled.toggle()
                }
            }, Supplier { "Toggle Device Enabled" })
        )
    }

    private fun addVolumeFaders() {
        val slider0 = hardwareSurface.createHardwareSlider("SLIDER_0")
        val slider1 = hardwareSurface.createHardwareSlider("SLIDER_1")
        val slider2 = hardwareSurface.createHardwareSlider("SLIDER_2")
        val slider3 = hardwareSurface.createHardwareSlider("SLIDER_3")

        slider0.setAdjustValueMatcher(inPort.createAbsoluteCCValueMatcher(0, FADER_0))
        slider1.setAdjustValueMatcher(inPort.createAbsoluteCCValueMatcher(0, FADER_1))
        slider2.setAdjustValueMatcher(inPort.createAbsoluteCCValueMatcher(0, FADER_2))
        slider3.setAdjustValueMatcher(inPort.createAbsoluteCCValueMatcher(0, FADER_3))

        slider0.setBinding(trackBank.getItemAt(0).volume())
        slider1.setBinding(trackBank.getItemAt(1).volume())
        slider2.setBinding(trackBank.getItemAt(2).volume())
        slider3.setBinding(trackBank.getItemAt(3).volume())
    }

    private fun addMutes() {
        // Define button MIDI notes
        val buttonNotes = arrayOf(BUTTON_3_0, BUTTON_3_1, BUTTON_3_2, BUTTON_3_3)

        // Iterate through the tracks in the track bank
        for (i in 0 until trackBank.sizeOfBank) {
            val track = trackBank.getItemAt(i)
            track.mute().markInterested()

            val muteButton = hardwareSurface.createHardwareButton("MUTE_BUTTON_$i")
            val muteButtonLight = hardwareSurface.createMultiStateHardwareLight("MUTE_BUTTON_LIGHT_$i")

            muteButton.setBackgroundLight(muteButtonLight)

            // Bind the button to toggle mute on the track
            muteButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, buttonNotes[i]))
            muteButton.pressedAction().setBinding(
                host.createAction(Runnable {
                    track.mute().toggle()
                }, Supplier { "Toggle Mute for Track $i" })
            )

            // Set LED feedback based on mute state
            muteButtonLight.state().setValueSupplier {
                LightState(
                    when {
                        track.mute().get() -> YELLOW
                        else -> OFF
                    }
                )
            }

            // Update the hardware LED when the state changes
            muteButtonLight.state().onUpdateHardware {
                val state = it as LightState
                hardware.updateLED(buttonNotes[i], state.color)
            }
        }
    }

    private fun addSolos() {
        // Define button MIDI notes
        val buttonNotes = arrayOf(BUTTON_2_0, BUTTON_2_1, BUTTON_2_2, BUTTON_2_3)

        // Iterate through the tracks in the track bank
        for (i in 0 until trackBank.sizeOfBank) {
            val track = trackBank.getItemAt(i)
            track.solo().markInterested()

            val soloButton = hardwareSurface.createHardwareButton("SOLO_BUTTON_$i")
            val soloButtonLight = hardwareSurface.createMultiStateHardwareLight("SOLO_BUTTON_LIGHT_$i")

            soloButton.setBackgroundLight(soloButtonLight)

            soloButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, buttonNotes[i]))
            soloButton.pressedAction().setBinding(
                host.createAction(Runnable {
                    track.solo().toggle()
                }, Supplier { "Toggle Solo for Track $i" })
            )

            soloButtonLight.state().setValueSupplier {
                LightState(
                    when {
                        track.solo().get() -> GREEN
                        else -> OFF
                    }
                )
            }

            // Update the hardware LED when the state changes
            soloButtonLight.state().onUpdateHardware {
                val state = it as LightState
                hardware.updateLED(buttonNotes[i], state.color)
            }
        }
    }

    private fun addArms() {
        // Define button MIDI notes
        val buttonNotes = arrayOf(BUTTON_1_0, BUTTON_1_1, BUTTON_1_2, BUTTON_1_3)

        // Iterate through the tracks in the track bank
        for (i in 0 until trackBank.sizeOfBank) {
            val track = trackBank.getItemAt(i)
            track.arm().markInterested()

            val armButton = hardwareSurface.createHardwareButton("ARM_BUTTON_$i")
            val armButtonLight = hardwareSurface.createMultiStateHardwareLight("ARM_BUTTON_LIGHT_$i")

            armButton.setBackgroundLight(armButtonLight)

            armButton.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, buttonNotes[i]))
            armButton.pressedAction().setBinding(
                host.createAction(Runnable {
                    track.arm().toggle()
                }, Supplier { "Toggle Arm for Track $i" })
            )

            armButtonLight.state().setValueSupplier {
                LightState(
                    when {
                        track.arm().get() -> RED
                        else -> OFF
                    }
                )
            }

            // Update the hardware LED when the state changes
            armButtonLight.state().onUpdateHardware {
                val state = it as LightState
                hardware.updateLED(buttonNotes[i], state.color)
            }
        }
    }

    private fun addPanners() {
        val rel0 = hardwareSurface.createRelativeHardwareKnob("REL_0")
        val rel1 = hardwareSurface.createRelativeHardwareKnob("REL_1")
        val rel2 = hardwareSurface.createRelativeHardwareKnob("REL_2")
        val rel3 = hardwareSurface.createRelativeHardwareKnob("REL_3")

        val rel0button = hardwareSurface.createHardwareButton("REL_0_CLICK")
        val rel1button = hardwareSurface.createHardwareButton("REL_1_CLICK")
        val rel2button = hardwareSurface.createHardwareButton("REL_2_CLICK")
        val rel3button = hardwareSurface.createHardwareButton("REL_3_CLICK")

        rel0.setAdjustValueMatcher(inPort.createRelative2sComplementCCValueMatcher(0, REL_0, 128))
        rel1.setAdjustValueMatcher(inPort.createRelative2sComplementCCValueMatcher(0, REL_1, 128))
        rel2.setAdjustValueMatcher(inPort.createRelative2sComplementCCValueMatcher(0, REL_2, 128))
        rel3.setAdjustValueMatcher(inPort.createRelative2sComplementCCValueMatcher(0, REL_3, 128))

        rel0button.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_0_CLICK))
        rel1button.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_1_CLICK))
        rel2button.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_2_CLICK))
        rel3button.pressedAction().setActionMatcher(inPort.createNoteOnActionMatcher(0, REL_3_CLICK))

        trackBank.getItemAt(0).pan().addBinding(rel0)
        trackBank.getItemAt(1).pan().addBinding(rel1)
        trackBank.getItemAt(2).pan().addBinding(rel2)
        trackBank.getItemAt(3).pan().addBinding(rel3)

        rel0button.pressedAction().setBinding(
            host.createAction(Runnable {
                trackBank.getItemAt(0).pan().reset()
            }, Supplier { "Reset Pan on Track 0" })
        )

        rel1button.pressedAction().setBinding(
            host.createAction(Runnable {
                trackBank.getItemAt(1).pan().reset()
            }, Supplier { "Reset Pan on Track 1" })
        )

        rel2button.pressedAction().setBinding(
            host.createAction(Runnable {
                trackBank.getItemAt(2).pan().reset()
            }, Supplier { "Reset Pan on Track 2" })
        )

        rel3button.pressedAction().setBinding(
            host.createAction(Runnable {
                trackBank.getItemAt(3).pan().reset()
            }, Supplier { "Reset Pan on Track 3" })
        )
    }

    private fun updateNavigation(mode: NavigationMode) {

        host.showPopupNotification(
            "${
                mode.name.lowercase()
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            } Mode"
        )

        verticalNavigation?.removeBinding()
        horizontalNavigation?.removeBinding()

        when (mode) {
            NavigationMode.SCENE -> {
                verticalNavigation = trackBank.addBinding(rel5)
                horizontalNavigation = sceneBank.addBinding(rel4)
                hardware.updateLED(BUTTON_LAYER, YELLOW)
            }

            NavigationMode.TRACK -> {
                verticalNavigation = cursorTrack.addBinding(rel5)
                horizontalNavigation = cursorDevice.addBinding(rel4)
                hardware.updateLED(BUTTON_LAYER, GREEN)
            }

            NavigationMode.DEVICE -> {
                verticalNavigation = remoteControlBank.addBinding(rel5)
                horizontalNavigation = cursorDevice.addBinding(rel4)
                hardware.updateLED(BUTTON_LAYER, RED)
            }
        }

        currentNavigationMode = mode
    }

    fun handleMidi(@Suppress("UNUSED_PARAMETER") msg: ShortMidiMessage): Boolean {
        return false
    }
}