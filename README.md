<p align="center">
  <img src="app/src/main/Dabbing logo-modified1.png" alt="Vibe cover" width="320">
  <br><br>
  <a href="https://github.com/amoeba115/vibe/releases/latest">
    <img src="https://img.shields.io/github/v/release/amoeba115/vibe?style=for-the-badge&logo=android&logoColor=white&label=Download%20APK&color=2EA44F" alt="Download Latest APK">
  </a>
</p>

# Vibe

Vibe is a refined/specialized fork of **AyraMusic**, an Android local music player. This fork keeps the original app’s clean local-first foundation, but where the original focused on VGM support, Vibe focuses on support for massive mp3 and FLAC libraries, as well as other standard audio file types. Vibe also includes optimized scanning, playlist support, and track info/metadata management, with a few other small tweaks here and there. 

## Features

- Optimized scanning/indexing flow for large local libraries
- Persistent cached library behavior to avoid full re-indexing on every launch
- Smart anti-repeat shuffle for more varied playback
- Faster, more reliable playlist import across mixed `.m3u`/`.m3u8` path styles
- Playlist import progress and success/failure feedback in Settings
- Play and Shuffle Play quick actions on album and playlist detail screens
- Track info panel from now-playing menu (file type, path, size, and more)
- Manual metadata editor from now-playing menu (title, artist, album, track/disc, year)
- VGM-specific support removed to simplify the app and focus local audio playback
- Updated branding/theme polish (name, icon, and accent refresh)


## Notes from Amoeba115

- I did all testing on my phone, a de-googled Motorola from a few years ago running Android 16. I unfortunately don't have any other devices to test on, so if it doesn't work for you, I'm sorry.
- Still, feel free to reach out if there's an issue, and I'll see what I can do to fix it!
- I built this because I'm a huge fan of Ayra's work with their Samsung Music port, but after de-googling my phone, that didn't work anymore. I saw that Ayra had AyraMusic, a standalone music player inspired by Samsung Music, and it worked, but I saw room to optimize it for my specific situation (huge library and de-googled phone) the way Ayra optimized it for their situation (lots of video game music)
- Nearly all credit goes to Ayra for the core scaffolding of this app, I just rewrote the scanning and indexing logic and made other small tweaks here and there.
