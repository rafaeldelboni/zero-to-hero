(ns game.core
  (:require
   ["phaser" :refer [AUTO Game Scale]]
   [game.interop :refer [debug?]]
   [game.preload :as preload]
   [game.test-level :as test-level]))

(defonce state (atom {}))

(def config {:type AUTO
             :parent "game"
             :width 400
             :height 300
             :pixelArt true
             :backgroundColor "#ffffff"
             :physics {:default "arcade"
                       :arcade {:gravity {:y 2000}
                                :debug debug?}}
             :scale {:mode (.-FIT Scale)
                     :autoCenter (.-CENTER_BOTH Scale)}
             :scene [{:key "preload"
                      :preload preload/game-preload}
                     {:key "test-level"
                      :create test-level/game-create
                      :update test-level/game-update}]})

(defn ^:export init []
  (when-let [^js/Object game (:game @state)]
    (.destroy game true))
  (reset! state {:game (new Game (clj->js config))}))

(init)
