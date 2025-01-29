(ns game.preload)

(defn preload! []
  (this-as ^js/Object this
    (-> this .-load
      (.aseprite "hero" "assets/sprites/hero.png" "assets/sprites/hero.json"))

    (-> this .-load
      (.spritesheet "monochrome-ss" "assets/monochrome_tilemap_packed.png" #js {:frameWidth 16 :frameHeight 16}))

    (-> this .-load
      (.on "complete"
           (fn []
             (-> this .-scene (.start "test-level")))))))
