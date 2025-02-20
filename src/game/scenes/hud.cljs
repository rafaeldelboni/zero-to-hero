(ns game.scenes.hud
  (:require
   [game.interop :refer [oassoc! oget]]
   [game.phaser.registry :as registry]
   [game.phaser.text :as text]))

(defn- update-heart! [ctx]
  (let [health (oget ctx :game/health)
        ^js/Object health-text (oget ctx :hud/health-text)]
    (-> health-text (.setText health))))

(defn- update-score! [ctx]
  (let [score (oget ctx :game/score)
        ^js/Object score-text (oget ctx :hud/score-text)]
    (-> score-text (.setText score))))

(defn- update-via-registry [^js/Object ctx _p k v]
  (oassoc! ctx k v)
  (case k
    :game/score (update-score! ctx)
    :game/health (update-heart! ctx)
    nil))

(defn create! []
  (this-as ^js/Object this
    (let [heart-icon (-> this .-add
                         (.sprite 10 10 "monochrome-ss" 41))

          health-text (text/set-text! this "-" {:x 20 :y 5
                                                :font "8px public-pixel"
                                                :fill "#ffffff"
                                                :stroke "#000000"
                                                :strokeThickness 2})
          diamond-icon (-> this .-add
                           (.sprite 47 10 "monochrome-ss" 22))
          score-text (text/set-text! this "-" {:x 57 :y 5
                                               :font "8px public-pixel"
                                               :fill "#ffffff"
                                               :stroke "#000000"
                                               :strokeThickness 2})]

      (oassoc! this :hud/health-text health-text)
      (oassoc! this :hud/score-text score-text)
      (oassoc! this :hud/heart-icon heart-icon)
      (oassoc! this :hud/diamond-icon diamond-icon)

      (registry/on-change! this update-via-registry))))
