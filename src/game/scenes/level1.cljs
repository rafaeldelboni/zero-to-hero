(ns game.scenes.level1
  (:require
   [game.interop :refer [oassoc!]]
   [game.level :as level]
   [game.player :as player]))

(defn preload! []
  (this-as ^js/Object this
    (-> this .-load
      (.tilemapTiledJSON "level-1" "assets/level-1.json"))))

(defn create! []
  (this-as ^js/Object this
    (let [player (player/create! this (* 16 4) (* 16 10))
          cursors (-> this .-input .-keyboard (.createCursorKeys))
          level (level/create-tiled-level! this player "level-1")]
      (level/create-camera! this player)
      (oassoc! this :level/player player)
      (oassoc! this :level/cursors cursors)
      (oassoc! this :level/current level))))

(defn update! []
  (this-as ^js/Object this
    (player/update! this)))
