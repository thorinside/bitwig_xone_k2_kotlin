# bitwig_xone_k2_kotlin

An Allen & Heath XONE:K2 Bitwig driver written in Kotlin. Primarily this controller script is useful for controlling the clip launcher for performance.

The A-P buttons display clips, pushing an orange clip will queue it for playback. Pushing an empty clip will queue it for recording if the track is armed. Pushing a green clip will stop the track from playing.

The sliders control track bank volume.

The relative buttons at the top of the controller control track pan. Pushing the button will return pan to center.

The relative buttons at the bottom of the controller move the trackbank and scene bank, respectively.

Exit Setup button starts and stops playback on the transport.

The lights at the top of the controller show transport play, record, fill enabled, and clip automation enabled, respectively.

TODO:

Want the clips to blink green or red if queued for playback or recording.
