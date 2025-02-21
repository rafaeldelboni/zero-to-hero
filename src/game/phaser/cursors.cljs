(ns game.phaser.cursors
  (:require
   ["phaser" :refer [Input]]))

(defn create!
  [^js/Object ctx]
  (-> ctx .-input .-keyboard
      (.addKeys "A,S,D,W,UP,DOWN,LEFT,RIGHT,SPACE,SHIFT")))

(defn left-is-pressed? [^js/Object cursors]
  (or (-> cursors .-A .-isDown)
      (-> cursors .-LEFT .-isDown)))

(defn right-is-pressed? [^js/Object cursors]
  (or (-> cursors .-D .-isDown)
      (-> cursors .-RIGHT .-isDown)))

(defn just-pressed? [keystoke]
  ((-> Input .-Keyboard .-JustDown) keystoke))

(defn attack-just-pressed? [^js/Object cursors]
  (just-pressed? (.-SHIFT cursors)))

(defn down-just-pressed? [^js/Object cursors]
  (or (just-pressed? (.-S cursors))
      (just-pressed? (.-DOWN cursors))))

(defn jump-just-pressed? [^js/Object cursors]
  (or (just-pressed? (.-SPACE cursors))
      (just-pressed? (.-UP cursors))
      (just-pressed? (.-W cursors))))
