(ns game.scenes.level1
  (:require
   [game.interop :refer [oassoc! oget]]
   [game.level :as level]
   [game.phaser.registry :as registry]
   [game.player :as player]
   [game.time :as time]))

(defn- update-final-score! [^js/Object ctx]
  (when (>= (registry/get! ctx :game/level) 4)
    (let [^js/Object game-time (oget ctx :level/final-score)
          start-time (registry/get! ctx :game/time)
          score (registry/get! ctx :game/score)
          current-time (time/now-seconds!)
          game-time-text (time/duration->string (- current-time start-time))]
      (.setText game-time
                (str "Game Over!\r\r"
                     "Time:  " game-time-text "\r"
                     "Score: " score " of 32 \r\r"
                     "Do it faster!")))))

(defn set-final-score! [^js/Object ctx]
  (-> ctx .-add
      (.text (* 16 96) (* 16 2) "" #js {:font "16px public-pixel"
                                        :fill "#ffffff"
                                        :stroke "#000000"
                                        :strokeThickness 2})))

(defn preload! []
  (this-as ^js/Object this
    (-> this .-load
      (.tilemapTiledJSON "level-1" "assets/level-1.json"))))

(defn create! []
  (this-as ^js/Object this
    (let [player (player/create! this (* 16 4) (* 16 10))
          cursors (-> this .-input .-keyboard (.createCursorKeys))
          level (level/create-tiled-level! this player "level-1")
          final-score (set-final-score! this)]
      (level/create-camera! this player)
      (oassoc! this :level/player player)
      (oassoc! this :level/cursors cursors)
      (oassoc! this :level/current level)
      (oassoc! this :level/final-score final-score))))

(defn update! []
  (this-as ^js/Object this
    (update-final-score! this)
    (player/update! this)))
