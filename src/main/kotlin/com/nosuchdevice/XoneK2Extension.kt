package com.nosuchdevice

import com.bitwig.extension.api.util.midi.ShortMidiMessage
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback
import com.bitwig.extension.controller.ControllerExtension
import com.bitwig.extension.controller.api.ControllerHost
import com.bitwig.extension.controller.api.HardwareSurface
import com.nosuchdevice.track.TrackHandler
import com.nosuchdevice.transport.TransportHandler

class XoneK2Extension(definition: XoneK2ExtensionDefinition, host: ControllerHost) :
    ControllerExtension(definition, host) {

    private var transportHandler: TransportHandler? = null
    private var trackHandler: TrackHandler? = null
    lateinit var hardwareSurface: HardwareSurface

    override fun init() {

        val inPort = host.getMidiInPort(0)
        val outPort = host.getMidiOutPort(0)
        hardwareSurface = host.createHardwareSurface()

        inPort.setMidiCallback(ShortMidiMessageReceivedCallback { msg: ShortMidiMessage -> onMidi0(msg) })

        val hardware = XoneK2Hardware(inPort, outPort)

        transportHandler = TransportHandler(
            inPort = inPort,
            transport = host.createTransport(),
            hardware = hardware,
            hardwareSurface = hardwareSurface,
        )

        trackHandler = TrackHandler(
            inPort = inPort,
            trackBank = host.createMainTrackBank(4, 0, 4),
            cursorTrack = host.createCursorTrack("XONE_CURSOR_TRACK", "Cursor Track", 0, 4, true),
            hardwareSurface = hardwareSurface,
            hardware = hardware,
            host = host
        )

        hardwareSurface.setPhysicalSize(100.0, 250.0)

        hardwareSurface.let { surface ->
            surface.hardwareElementWithId("PLAY_BUTTON").setBounds(85.5, 234.25, 10.0, 10.0)
            surface.hardwareElementWithId("SLIDER_0").setBounds(28.25, 105.75, 10.0, 50.0)
            surface.hardwareElementWithId("SLIDER_1").setBounds(40.25, 105.75, 10.0, 50.0)
            surface.hardwareElementWithId("SLIDER_2").setBounds(52.25, 105.75, 10.0, 50.0)
            surface.hardwareElementWithId("SLIDER_3").setBounds(65.75, 105.75, 10.0, 50.0)
            surface.hardwareElementWithId("REL_0").setBounds(27.25, 6.5, 10.0, 10.0)
            surface.hardwareElementWithId("REL_1").setBounds(39.25, 6.5, 10.0, 10.0)
            surface.hardwareElementWithId("REL_2").setBounds(51.25, 6.5, 10.0, 10.0)
            surface.hardwareElementWithId("REL_3").setBounds(63.25, 6.5, 10.0, 10.0)
            surface.hardwareElementWithId("REL_0_CLICK").setBounds(27.75, 18.75, 10.0, 10.0)
            surface.hardwareElementWithId("REL_1_CLICK").setBounds(39.75, 18.75, 10.0, 10.0)
            surface.hardwareElementWithId("REL_2_CLICK").setBounds(51.75, 18.75, 10.0, 10.0)
            surface.hardwareElementWithId("REL_3_CLICK").setBounds(64.0, 18.5, 10.0, 10.0)
            surface.hardwareElementWithId("REL_4").setBounds(41.0, 233.0, 10.0, 10.0)
            surface.hardwareElementWithId("REL_5").setBounds(55.75, 233.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_0_0").setBounds(27.5, 170.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_0_1").setBounds(39.5, 170.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_0_2").setBounds(51.5, 170.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_0_3").setBounds(63.5, 170.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_1_0").setBounds(27.75, 181.5, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_1_1").setBounds(39.5, 181.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_1_2").setBounds(51.5, 181.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_1_3").setBounds(63.5, 181.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_2_0").setBounds(28.0, 193.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_2_1").setBounds(40.0, 193.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_2_2").setBounds(52.0, 193.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_2_3").setBounds(64.0, 193.75, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_3_0").setBounds(28.25, 206.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_3_1").setBounds(40.5, 206.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_3_2").setBounds(52.5, 206.0, 10.0, 10.0)
            surface.hardwareElementWithId("PLAY_BUTTON_3_3").setBounds(64.5, 206.0, 10.0, 10.0)
            surface.hardwareElementWithId("LIGHT_0").setBounds(20.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("LIGHT_1").setBounds(38.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("LIGHT_2").setBounds(55.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("LIGHT_3").setBounds(55.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_0").setBounds(20.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_1").setBounds(38.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_2").setBounds(55.25, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_3").setBounds(69.5, 43.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_4").setBounds(20.25, 60.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_5").setBounds(38.25, 60.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_6").setBounds(55.25, 60.75, 10.0, 10.0)
            surface.hardwareElementWithId("KNOB_7").setBounds(69.5, 60.75, 10.0, 10.0)

            surface.hardwareElementWithId("BUTTON_8").setBounds(15.5, 170.0, 10.0, 10.0)
            //surface.hardwareElementWithId("KNOB_8").setBounds(20.25, 78.75, 10.0, 10.0)
            //surface.hardwareElementWithId("KNOB_9").setBounds(38.25, 78.75, 10.0, 10.0)
            //surface.hardwareElementWithId("KNOB_10").setBounds(55.25, 78.75, 10.0, 10.0)
            //surface.hardwareElementWithId("KNOB_11").setBounds(69.5, 78.75, 10.0, 10.0)
        }

        host.showPopupNotification("Xone:K2 Initialized")
    }

    override fun exit() {
        // TODO: Perform any cleanup once the driver exits
        // For now just show a popup notification for verification that it is no longer running.
        host.showPopupNotification("XoneK2 Exited")
    }

    override fun flush() {
        hardwareSurface.updateHardware()
    }

    /** Called when we receive short MIDI message on port 0.  */
    private fun onMidi0(msg: ShortMidiMessage) {
        if (trackHandler?.handleMidi(msg) == true) return

        host.println(msg.toString())
    }
}
