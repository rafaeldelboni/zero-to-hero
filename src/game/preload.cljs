(ns game.preload)

(defn game-preload []
  (this-as ^js/Object this
    (-> this .-load
      (.aseprite "hero" "assets/sprites/hero.png" "assets/sprites/hero.json"))

    (-> this .-load
      (.image "monochrome-sheet" "assets/monochrome_tilemap_packed.png"))

    (-> this .-load
      (.on "complete"
           (fn []
             (-> this .-scene (.start "test-level")))))))
