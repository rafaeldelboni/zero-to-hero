(ns game.core
  (:require
   ["phaser" :refer [AUTO Game Scale]]
   [game.hud :as hud]
   [game.interop :refer [debug?]]
   [game.preload :as preload]
   [game.test-level :as test-level]))

(defonce state (atom {}))

(def config {:type AUTO
             :parent "game"
             :width 400
             :height 300
             :pixelArt true
             :audio {:disableWebAudio true} ;; TODO
             :physics {:default "arcade"
                       :arcade {:gravity {:y 2000}
                                :debug debug?}}
             :scale {:mode (.-FIT Scale)
                     :autoCenter (.-CENTER_BOTH Scale)}
             :scene [{:key "preload"
                      :preload preload/preload!}
                     {:key "test-level"
                      :preload test-level/preload!
                      :create test-level/create!
                      :update test-level/update!}
                     {:key "hud"
                      :create hud/create!}]})

(defn ^:export init []
  (when-let [^js/Object game (:game @state)]
    (.destroy game true))
  (reset! state {:game (new Game (clj->js config))}))

(init)
