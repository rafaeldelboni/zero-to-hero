# Audio
wav -> ogg

```bash
for f in *.wav; do ffmpeg -y -i "$f" -c:a libvorbis -q:a 4 "../../../public/assets/audio/${f%.*}.ogg"; done
```

trim move sfx and convert to ogg on assets
```bash
ffmpeg -i move.wav -t 00:00:00.250 -c copy move-trim.wav
ffmpeg -i move-trim.wav -c:a libvorbis -q:a 4 "../../../public/assets/audio/move.ogg"
```
