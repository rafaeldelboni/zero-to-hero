(ns game.scenes.hud
  (:require
   [game.interop :refer [oassoc! oget]]))

(defn- update-heart! [ctx]
  (let [health (oget ctx :game/health)
        ^js/Object heart-indicator-3 (oget ctx :hud/heart-icon-3)
        ^js/Object heart-indicator-2 (oget ctx :hud/heart-icon-2)
        ^js/Object heart-indicator-1 (oget ctx :hud/heart-icon-1)]
    (case health
      3 (do (.setVisible heart-indicator-3 true)
            (.setVisible heart-indicator-2 false)
            (.setVisible heart-indicator-1 false))
      2 (do (.setVisible heart-indicator-3 false)
            (.setVisible heart-indicator-2 true)
            (.setVisible heart-indicator-1 false))
      1 (do (.setVisible heart-indicator-3 false)
            (.setVisible heart-indicator-2 false)
            (.setVisible heart-indicator-1 true))
      (do (.setVisible heart-indicator-3 false)
          (.setVisible heart-indicator-2 false)
          (.setVisible heart-indicator-1 false)))))

(defn- update-score! [ctx]
  (let [score (oget ctx :game/score 0)
        ^js/Object score-text (oget ctx :hud/score-text)]
    (-> score-text (.setText score))))

(defn- update-via-registry [this _parent k v]
  (oassoc! this k v)
  (case k
    "game/score" (update-score! this)
    "game/health" (update-heart! this)))

(defn create! []
  (this-as ^js/Object this
    (let [screen-width (-> this .-sys .-game .-canvas .-width)
          score-text (-> this .-add
                         (.text 5 5 "-" #js {:font "8px public-pixel"
                                             :fill "#ffffff"
                                             :stroke "#000000"
                                             :strokeThickness 2}))
          heart-icon-3 (-> this .-add
                           (.sprite (- screen-width 10) 10 "monochrome-ss" 42))
          heart-icon-2 (-> this .-add
                           (.sprite (- screen-width 10) 10 "monochrome-ss" 41))
          heart-icon-1 (-> this .-add
                           (.sprite (- screen-width 10) 10 "monochrome-ss" 40))]

      (oassoc! this :hud/score-text score-text)
      (oassoc! this :hud/heart-icon-3 heart-icon-3)
      (oassoc! this :hud/heart-icon-2 heart-icon-2)
      (oassoc! this :hud/heart-icon-1 heart-icon-1)

      (-> this .-registry
          (.each (fn [p k v] (update-via-registry this p k v))))
      (-> this .-registry .-events
          (.on "changedata" (partial update-via-registry this) this)))))
