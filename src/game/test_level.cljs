(ns game.test-level
  (:require
   [game.interop :refer [oassoc!]]
   [game.player :as player]))

(defn game-create []
  (this-as ^js/Object this
    (oassoc! this :player (player/game-create this))
    (oassoc! this :cursors (-> this .-input .-keyboard (.createCursorKeys)))))

(defn game-update []
  (this-as ^js/Object this
    (player/game-update this)))
