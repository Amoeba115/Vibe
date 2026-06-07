# TODO
- Refactor UI structure of mini player and fullscreen player:
MusicApp
 ├─ MainScreen
 └─ PlayerSheet
With state:
enum class PlayerSheetState {
    Collapsed,
    Expanded
}
And shared cover:
SharedTransitionLayout {
    sharedElement(...)
}
- Refactor ExpandedPlayerScreen to:
PlayerSheet
├─ CollapsedPlayerContent
├─ ExpandedPlayerContent
├─ SeekBar
└─ Controls
