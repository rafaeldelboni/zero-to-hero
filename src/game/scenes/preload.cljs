(ns game.scenes.preload
  (:require
   [game.interop :refer [oassoc! oget]]
   [game.phaser.cursors :as cursors]
   [game.phaser.registry :as registry]
   [game.phaser.text :as text]
   [game.time :as time]))

(defn- load-font [^js/Object ctx name url]
  (let [new-font (new js/FontFace name (str "url(" url ")"))]
    (-> new-font .load
        (.then (fn [loaded]
                 (oassoc! ctx :load/font true)
                 (-> js/document .-fonts (.add loaded))))
        (.catch (fn [error]
                  (js/console.error error))))))

(defn preload! []
  (this-as ^js/Object this
    (oassoc! this :load/assets false)
    (oassoc! this :load/font false)

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
             (oassoc! this :load/assets true))))))

(defn create! []
  (this-as ^js/Object this
    (let [loading-text (text/set-text! this "loading..."
                                       {:x 150 :y 240
                                        :alpha 1
                                        :font "8px public-pixel"
                                        :fill "#cccccc"})
          start-text (text/set-text! this "press [space] to start!"
                                     {:x 120 :y 240
                                      :alpha 0
                                      :font "8px public-pixel"
                                      :fill "#cccccc"})]
      (-> this .-add (.image 200 140 "logo"))
      (-> this .-add (.image 200 60 "logo-text"))
      (oassoc! this :load/loading-text loading-text)
      (oassoc! this :load/start-text start-text)
      (registry/set! this :game/health 3)
      (registry/set! this :game/score 0)
      (registry/set! this :game/level 0)
      (registry/set! this :game/time (time/now-seconds!)))))

(defn update! []
  (this-as ^js/Object this
    (let [cursors (-> this .-input .-keyboard (.createCursorKeys))]
      (when (and (oget this :load/font)
                 (oget this :load/assets))
        (.setAlpha ^js/Object (oget this :load/loading-text) 0)
        (.setAlpha ^js/Object (oget this :load/start-text) 1)
        (when (cursors/attack-just-pressed? cursors)
          (-> this .-scene (.start "hud"))
          (-> this .-scene (.start "level-1")))))))
