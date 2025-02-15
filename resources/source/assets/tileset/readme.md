# Extrude Tiles

```bash
npx tile-extruder --tileWidth 16 --tileHeight 16 --input monochrome_tilemap_packed.png --output ../../../public/assets/monochrome_tilemap_extruded.png
```

> TLDR is that there are many ways this type of rendering artifact can occur, e.g. from pixels being incorrectly blended when the edge of a tile in a tilemap texture is rendered. This CLI app handles both tilesets with and without margin & spacing.
[source](https://github.com/sporadic-labs/tile-extruder)
