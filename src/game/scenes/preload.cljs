(ns game.scenes.preload)

(defn- load-font [name url]
  (let [new-font (new js/FontFace name (str "url(" url ")"))]
    (-> new-font .load
        (.then (fn [loaded]
                 (-> js/document .-fonts (.add loaded))))
        (.catch (fn [error]
                  (js/console.error error))))))

(defn preload! []
  (this-as ^js/Object this
    (load-font "public-pixel" "assets/PublicPixel.ttf")

    (-> this .-load
      (.aseprite "hero"
                 "assets/sprites/hero.png"
                 "assets/sprites/hero.json"))

    (-> this .-load
      (.spritesheet "monochrome-ss"
                    "assets/monochrome_tilemap_packed.png"
                    #js {:frameWidth 16 :frameHeight 16}))

    (-> this .-load
      (.on "complete"
           (fn []
             (-> this .-scene (.start "hud"))
             (-> this .-scene (.start "test-level")))))))

(defn create! []
  (this-as ^js/Object this
    (-> this .-registry (.inc "game/health" 3))
    (-> this .-registry (.inc "game/score" 0))))
