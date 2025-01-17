# Zero to Hero
Game made for the [First Dev Jam 2025](https://docs.google.com/spreadsheets/d/1Zl4_gNtujm9f759oWaeIs22dplejp5yB_J1pHmns72I/edit?gid=324790356#gid=324790356)

## Requirements
- [npm](https://www.npmjs.com/)
- [clojure](https://clojure.org/)

## Instructions

### Bootstrap
```bash
npm install
```

### Watch on http://localhost:5000
```bash
npm run watch
```

### Release Build (release/public)
```bash
npm run release
```

#### Serve Release bundle
```bash
gzip resources/public/js/*.js
npx http-server resources/public -g
```

## Tools
- [aseprite](https://www.aseprite.org/)
- [tiled](https://www.mapeditor.org/)

## Credits
- https://kenney.nl/assets/1-bit-platformer-pack
