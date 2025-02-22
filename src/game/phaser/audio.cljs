(ns game.phaser.audio
  (:require
   [game.interop :refer [oget]]))

(def sfx-opts {:volume 0.3})

(defn add!
  ([^js/Object ctx sound]
   (add! ctx sound {}))
  ([^js/Object ctx sound opts]
   (-> ctx .-sound (.add sound (clj->js (merge sfx-opts opts))))))

(defn key-play!
  ([^js/Object ctx k]
   (key-play! ctx k false))
  ([^js/Object ctx k wait?]
   (let [^js/Object audio (oget ctx k)]
     (when (or (not wait?) (not (.-isPlaying audio)))
       (.play audio)))))

(defn key-toggle!
  [^js/Object ctx k]
  (let [^js/Object audio (oget ctx k)]
    (if (.-isPlaying audio)
      (.stop audio)
      (.play audio))))
