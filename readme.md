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

## TODO
- [x] **Handle blob transformation**
    - [x] Play animation 
    - [x] Resize body (physics)
    - [x] Don't let player change forms if inside tunel
- [ ] **Push boxes**
    - [ ] Don't let player change forms if above pushable
    - [ ] Find pushable object sprite in tiled spritesheet
    - [ ] Fix jumping? when over pushable
    - [ ] Play animation
- [ ] **Attack destructibles**
    - [ ] Play animation
    - [ ] Collision + effect
- [ ] **Interact with levers**
- [ ] **Pickups**
- [ ] **Receive damage**
- [ ] **Consider current form & level to allow/deny actions like jump, push, attack**

## Tools
- [aseprite](https://www.aseprite.org/)
- [tiled](https://www.mapeditor.org/)

## Credits
- https://kenney.nl/assets/1-bit-platformer-pack
