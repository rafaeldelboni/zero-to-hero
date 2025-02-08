(ns game.phaser.anims)

(defn generate-frame-numbers [^js/Object scene source-name opts]
  (-> scene .-anims (.generateFrameNumbers source-name (clj->js opts))))

(defn generate-frame-names [^js/Object scene source-name opts]
  (-> scene .-anims (.generateFrameNames source-name (clj->js opts))))

(defn create! [^js/Object scene opts]
  (-> scene .-anims
      (.create (clj->js opts))))
