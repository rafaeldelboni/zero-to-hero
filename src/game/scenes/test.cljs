(ns game.scenes.test
  (:require
   [game.interop :refer [oassoc!]]
   [game.level :as level]
   [game.phaser.cursors :as cursors]
   [game.player :as player]))

(defn preload! []
  (this-as ^js/Object this
    (-> this .-load
      (.tilemapTiledJSON "test-level" "assets/test-level.json"))))

(defn create! []
  (this-as ^js/Object this
    (let [player (player/create! this 200 200)
          cursors (cursors/create! this)
          level (level/create-tiled-level! this player "test-level")]
      (level/create-camera! this player)
      (oassoc! this :level/player player)
      (oassoc! this :level/cursors cursors)
      (oassoc! this :level/current level))))

(defn update! []
  (this-as ^js/Object this
    (player/update! this)))
