package com.nosuchdevice

import com.bitwig.extension.controller.api.MidiIn
import com.bitwig.extension.controller.api.MidiOut
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class XoneK2Hardware(private val inputPort: MidiIn, private val outputPort: MidiOut) : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val blinkMap: MutableMap<Int, Job> = mutableMapOf()

    fun getLEDFor(i: Int, j: Int): Int {
        return GRID[i][j]
    }

    fun updateLED(note: Int, colour: Int) {
        val noteOffset = if (note == 0xf || note == 0xc) 4 else 36

        when (colour) {
            OFF -> {
                outputPort.sendMidi(0x90, note, 0)
            }

            RED -> {
                outputPort.sendMidi(0x90, note, 127)
            }

            GREEN -> {
                outputPort.sendMidi(0x90, note + noteOffset * 2, 127)
            }

            YELLOW -> {
                outputPort.sendMidi(0x90, note + noteOffset, 127)
            }
        }
    }

    fun blinkLED(buttonNote: Int) {
        blinkMap[buttonNote]?.cancel()
        blinkMap[buttonNote] = launch {
            while (isActive) {
                updateLED(buttonNote, GREEN)
                delay(150)
                updateLED(buttonNote, YELLOW)
                delay(500)
            }
        }
    }

    fun cancelBlink(buttonNote: Int) {
        blinkMap.remove(buttonNote)?.cancel()
    }

    companion object {
        const val REL_0_CLICK = 0x34
        const val REL_1_CLICK = 0x35
        const val REL_2_CLICK = 0x36
        const val REL_3_CLICK = 0x37

        const val ABS_0 = 4

        const val FADER_0 = 0x10
        const val FADER_1 = 0x11
        const val FADER_2 = 0x12
        const val FADER_3 = 0x13

        const val BUTTON_3_0 = 0x28
        const val BUTTON_3_1 = 0x29
        const val BUTTON_3_2 = 0x2a
        const val BUTTON_3_3 = 0x2b

        const val BUTTON_A = 0x24
        const val BUTTON_B = 0x25
        const val BUTTON_C = 0x26
        const val BUTTON_D = 0x27

        const val BUTTON_E = 0x20
        const val BUTTON_F = 0x21
        const val BUTTON_G = 0x22
        const val BUTTON_H = 0x23

        const val BUTTON_I = 0x1c
        const val BUTTON_J = 0x1d
        const val BUTTON_K = 0x1e
        const val BUTTON_L = 0x1f

        const val BUTTON_M = 0x18
        const val BUTTON_N = 0x19
        const val BUTTON_O = 0x1a
        const val BUTTON_P = 0x1b

        const val BUTTON_LAYER = 0x0c
        const val BUTTON_SETUP = 0x0f

        const val REL_0 = 0x00
        const val REL_1 = 0x01
        const val REL_2 = 0x02
        const val REL_3 = 0x03
        const val REL_4 = 0x14
        const val REL_5 = 0x15

        const val REL_4_BUTTON = 13
        const val REL_5_BUTTON = 14

        const val LIGHT_0 = 52
        const val LIGHT_1 = 53
        const val LIGHT_2 = 54
        const val LIGHT_3 = 55

        const val RED = 1
        const val YELLOW = 2
        const val GREEN = 3
        const val BLINK_GREEN = 4
        const val OFF = 0

        val GRID = arrayOf(
            arrayOf(BUTTON_A, BUTTON_B, BUTTON_C, BUTTON_D),
            arrayOf(BUTTON_E, BUTTON_F, BUTTON_G, BUTTON_H),
            arrayOf(BUTTON_I, BUTTON_J, BUTTON_K, BUTTON_L),
            arrayOf(BUTTON_M, BUTTON_N, BUTTON_O, BUTTON_P),
        )

        val LETTER_BUTTON_COORDINATES = mapOf(
            BUTTON_A to arrayOf(0, 0),
            BUTTON_B to arrayOf(0, 1),
            BUTTON_C to arrayOf(0, 2),
            BUTTON_D to arrayOf(0, 3),
            BUTTON_E to arrayOf(1, 0),
            BUTTON_F to arrayOf(1, 1),
            BUTTON_G to arrayOf(1, 2),
            BUTTON_H to arrayOf(1, 3),
            BUTTON_I to arrayOf(2, 0),
            BUTTON_J to arrayOf(2, 1),
            BUTTON_K to arrayOf(2, 2),
            BUTTON_L to arrayOf(2, 3),
            BUTTON_M to arrayOf(3, 0),
            BUTTON_N to arrayOf(3, 1),
            BUTTON_O to arrayOf(3, 2),
            BUTTON_P to arrayOf(3, 3),
        )
    }
}