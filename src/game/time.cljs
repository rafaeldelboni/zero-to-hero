(ns game.time
  (:require
   [cljs.math :as math]))

(defn now-seconds! []
  (math/round (/ (.now js/Date) 1000)))

(defn duration->string [duration]
  (let [hrs (math/round (/ duration 3600))
        mins (math/round (/ (math/floor-mod duration 3600) 60))
        secs (math/round (math/floor-mod duration 60))]
    (cond-> ""
      (> hrs 0) (str hrs ":" (if (< mins 10) "0" ""))
      :always (str mins ":" (if (< secs 10) "0" "") secs))))

(comment
  (= (duration->string 8)       "0:08")
  (= (duration->string 68)      "1:08")
  (= (duration->string 1768)    "29:28")
  (= (duration->string 3600)    "1:00:00")
  (= (duration->string 5296)    "1:28:16")
  (= (duration->string 7735)    "2:09:55")
  (= (duration->string 45296)   "13:35:56")
  (= (duration->string 145296)  "40:22:36")
  (= (duration->string 1145296) "318:08:16"))
