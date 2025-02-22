(ns game.scenes.preload
  (:require
   [game.phaser.registry :as registry]))

(defn- load-font [^js/Object ctx name url]
  (let [new-font (new js/FontFace name (str "url(" url ")"))]
    (-> new-font .load
        (.then (fn [loaded]
                 (registry/set! ctx :load/font true)
                 (-> js/document .-fonts (.add loaded))))
        (.catch (fn [error]
                  (js/console.error error))))))

(defn preload! []
  (this-as ^js/Object this
    (registry/set! this :load/font false)

    (load-font this "public-pixel" "assets/PublicPixel.ttf")

    (-> this .-load (.audio "the-vapours" "assets/audio/the-vapours.ogg"))
    (-> this .-load (.audio "attack" "assets/audio/attack.ogg"))
    (-> this .-load (.audio "coin" "assets/audio/coin.ogg"))
    (-> this .-load (.audio "damage" "assets/audio/damage.ogg"))
    (-> this .-load (.audio "destroy" "assets/audio/destroy.ogg"))
    (-> this .-load (.audio "game-over" "assets/audio/game-over.ogg"))
    (-> this .-load (.audio "health" "assets/audio/health.ogg"))
    (-> this .-load (.audio "jump" "assets/audio/jump.ogg"))
    (-> this .-load (.audio "move" "assets/audio/move.ogg"))
    (-> this .-load (.audio "orb" "assets/audio/orb.ogg"))
    (-> this .-load (.audio "toggle" "assets/audio/toggle.ogg"))

    (-> this .-load (.image "logo" "assets/logo.png"))
    (-> this .-load (.image "logo-text" "assets/logo-text.png"))

    (-> this .-load
      (.aseprite "hero"
                 "assets/sprites/hero.png"
                 "assets/sprites/hero.json"))

    (-> this .-load
      (.spritesheet "monochrome-ss"
                    "assets/monochrome_tilemap_extruded.png"
                    #js {:frameWidth 16 :frameHeight 16 :margin 1 :spacing 2}))

    (-> this .-load
      (.on "complete"
           (fn []
             (-> this .-scene (.start "main-menu")))))))
