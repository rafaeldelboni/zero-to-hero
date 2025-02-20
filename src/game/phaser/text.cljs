(ns game.phaser.text)

(defn set-text! [^js/Object ctx value {:keys [x y alpha] :as opts}]
  (let [text (-> ctx .-add (.text x y value (clj->js (dissoc opts :alpha :x :y))))]
    (.setAlpha text (or alpha 1))
    text))
