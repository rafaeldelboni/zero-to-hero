(ns game.phaser.cursors
  (:require
   ["phaser" :refer [Input]]))

(defn left-is-pressed? [^js/Object cursors]
  (-> cursors .-left .-isDown))

(defn right-is-pressed? [^js/Object cursors]
  (-> cursors .-right .-isDown))

(defn just-pressed? [keystoke]
  ((-> Input .-Keyboard .-JustDown) keystoke))

(defn attack-just-pressed? [^js/Object cursors]
  (just-pressed? (.-space cursors)))

(defn down-just-pressed? [^js/Object cursors]
  (just-pressed? (.-down cursors)))

(defn up-just-pressed? [^js/Object cursors]
  (just-pressed? (.-up cursors)))
