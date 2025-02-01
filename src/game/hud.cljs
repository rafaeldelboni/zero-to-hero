(ns game.hud
  (:require
   [game.interop :refer [oassoc! oget]]))

(defn- update-heart! [ctx]
  (let [health (oget ctx :health 3)
        ^js/Object heart-indicator-3 (oget ctx :heart-hud-3)
        ^js/Object heart-indicator-2 (oget ctx :heart-hud-2)
        ^js/Object heart-indicator-1 (oget ctx :heart-hud-1)]
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
  (let [score (oget ctx :score 0)
        ^js/Object score-text (oget ctx :score-hud)]
    (-> score-text (.setText score))))

(defn create! []
  (this-as ^js/Object this
    (let [screen-width (-> this .-sys .-game .-canvas .-width)
          score-text (-> this .-add
                         (.text 5 5 "-" #js {:font "8px public-pixel" :fill "#ffffff"}))
          heart-indicator-3 (-> this .-add
                                (.sprite (- screen-width 10) 10 "monochrome-ss" 42))
          heart-indicator-2 (-> this .-add
                                (.sprite (- screen-width 10) 10 "monochrome-ss" 41))
          heart-indicator-1 (-> this .-add
                                (.sprite (- screen-width 10) 10 "monochrome-ss" 40))]
      (oassoc! this :score-hud score-text)
      (oassoc! this :heart-hud-3 heart-indicator-3)
      (oassoc! this :heart-hud-2 heart-indicator-2)
      (oassoc! this :heart-hud-1 heart-indicator-1)

      (update-score! this)
      (update-heart! this))))
