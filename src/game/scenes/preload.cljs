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
