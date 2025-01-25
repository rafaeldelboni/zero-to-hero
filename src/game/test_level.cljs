(ns game.test-level
  (:require
   [game.interop :refer [oassoc!]]
   [game.player :as player]))

(defn preload! []
  (this-as ^js/Object this
    (-> this .-load
      (.tilemapTiledJSON "level1" "assets/level1.json"))))

(defn create! []
  (this-as ^js/Object this
    (let [player (player/create! this)
          cursors (-> this .-input .-keyboard (.createCursorKeys))
          level (-> this .-make (.tilemap #js {:key "level1"}))
          tileset (.addTilesetImage level "monochrome" "monochrome-sheet")
          ground (.createLayer level "ground" tileset)]
      (.setCollisionByProperty ground #js {:collides true})
      (-> this .-physics .-add (.collider player ground))

      (oassoc! this :player player)
      (oassoc! this :cursors cursors)
      (oassoc! this :level level))))

(defn update! []
  (this-as ^js/Object this
    (player/update! this)))
