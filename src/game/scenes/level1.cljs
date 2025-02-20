(ns game.scenes.level1
  (:require
   [game.interop :refer [oassoc! oget]]
   [game.level :as level]
   [game.phaser.registry :as registry]
   [game.phaser.text :as text]
   [game.player :as player]
   [game.time :as time]))

(defn score-msg! [^js/Object ctx]
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

(defn level-msgs! [^js/Object ctx {:keys [msg-key x y size alpha msg-value]}]
  (when-not (oget ctx msg-key)
    (let [text (text/set-text! ctx msg-value {:x (* 16 x) :y (* 16 y)
                                              :alpha alpha
                                              :font (str size "px public-pixel")
                                              :fill "#ffffff"
                                              :stroke "#000000"
                                              :strokeThickness 1})]
      (oassoc! ctx :level/level-1-msg text)))
  ctx)

(defn level-1-msg! [^js/Object ctx]
  (level-msgs! ctx {:x 6 :y 28.5
                    :size 8 :alpha 0.25
                    :msg-key :level/level-1-msg
                    :msg-value (str "    Press [down]\r"
                                    "to toggle your form")}))

(defn level-2-msg! [^js/Object ctx]
  (level-msgs! ctx
               {:x 82.25 :y 18
                :size 8 :alpha 0.25
                :msg-key :level/level-2-msg
                :msg-value "Arms can push"}))

(defn level-3-msg! [^js/Object ctx]
  (level-msgs! ctx
               {:x 82.5 :y 28
                :size 8 :alpha 0.25
                :msg-key :level/level-3-msg
                :msg-value "You see more"}))

(defn level-4-msg! [^js/Object ctx]
  (level-msgs! ctx
               {:x 7.5 :y 38.5
                :size 8 :alpha 0.25
                :msg-key :level/level-4-msg
                :msg-value (str "Press [space]\r"
                                "to use sword")}))

(defn- update-msgs! [^js/Object ctx _p k v]
  (when (= k :game/level)
    (let [level v]
      (cond-> ctx
        (>= level 1) level-1-msg!
        (>= level 2) level-2-msg!
        (>= level 3) level-3-msg!
        (>= level 4) level-4-msg!))))

(defn set-final-score! [^js/Object ctx]
  (text/set-text! ctx "" {:x (* 16 96) :y (* 16 2)
                          :font "16px public-pixel"
                          :fill "#ffffff"
                          :stroke "#000000"
                          :strokeThickness 2}))

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
      (registry/on-change! this update-msgs!)
      (level/create-camera! this player)
      (oassoc! this :level/player player)
      (oassoc! this :level/cursors cursors)
      (oassoc! this :level/current level)
      (oassoc! this :level/final-score final-score))))

(defn update! []
  (this-as ^js/Object this
    (score-msg! this)
    (player/update! this)))
