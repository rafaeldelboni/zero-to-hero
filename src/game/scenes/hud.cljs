(ns game.scenes.hud
  (:require
   [game.interop :refer [oassoc! oget]]))

(defn- update-heart! [ctx]
  (let [health (oget ctx :game/health)
        ^js/Object health-text (oget ctx :hud/health-text)]
    (-> health-text (.setText health))))

(defn- update-score! [ctx]
  (let [score (oget ctx :game/score)
        ^js/Object score-text (oget ctx :hud/score-text)]
    (-> score-text (.setText score))))

(defn- update-via-registry [this _parent k v]
  (oassoc! this k v)
  (case k
    "game/score" (update-score! this)
    "game/health" (update-heart! this)))

(defn create! []
  (this-as ^js/Object this
    (let [heart-icon (-> this .-add
                         (.sprite 10 10 "monochrome-ss" 41))

          health-text (-> this .-add
                          (.text 20 5 "-" #js {:font "8px public-pixel"
                                               :fill "#ffffff"
                                               :stroke "#000000"
                                               :strokeThickness 2}))
          diamond-icon (-> this .-add
                           (.sprite 47 10 "monochrome-ss" 22))
          score-text (-> this .-add
                         (.text 57 5 "-" #js {:font "8px public-pixel"
                                              :fill "#ffffff"
                                              :stroke "#000000"
                                              :strokeThickness 2}))]

      (oassoc! this :hud/health-text health-text)
      (oassoc! this :hud/score-text score-text)
      (oassoc! this :hud/heart-icon heart-icon)
      (oassoc! this :hud/diamond-icon diamond-icon)

      (-> this .-registry
          (.each (fn [p k v] (update-via-registry this p k v))))
      (-> this .-registry .-events
          (.on "changedata" (partial update-via-registry this) this)))))
