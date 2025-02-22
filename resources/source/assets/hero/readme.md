# Spritesheet
aseprite -> json spritesheet

```bash
aseprite -b --split-layers *.aseprite --filename-format "{title}-{layer}-{frame}" --tagname-format "{tag}" --format json-array --list-tags --sheet-pack --sheet ../../../public/assets/sprites/hero.png --data ../../../public/assets/sprites/hero.json
```
