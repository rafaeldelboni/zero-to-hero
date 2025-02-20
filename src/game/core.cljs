(ns game.core
  (:require
   ["phaser" :refer [AUTO Game Scale]]
   [game.interop :refer [debug?]]
   [game.scenes.hud :as scene.hud]
   [game.scenes.level1 :as scene.level1]
   [game.scenes.preload :as scene.preload]
   [game.scenes.test :as scene.test]))

(defonce state (atom {}))

(def config {:type AUTO
             :parent "game"
             :width 400
             :height 300
             :pixelArt true
             :audio {:disableWebAudio true} ;; TODO
             :physics {:default "arcade"
                       :arcade {:fixedStep false
                                :gravity {:y 2000}
                                :debug debug?}}
             :scale {:autoCenter (.-CENTER_BOTH Scale)
                     :mode (.-FIT Scale)}
             :scene [{:key "preload"
                      :preload scene.preload/preload!
                      :create scene.preload/create!
                      :update scene.preload/update!}
                     {:key "test-level"
                      :preload scene.test/preload!
                      :create scene.test/create!
                      :update scene.test/update!}
                     {:key "level-1"
                      :preload scene.level1/preload!
                      :create scene.level1/create!
                      :update scene.level1/update!}
                     {:key "hud"
                      :create scene.hud/create!}]})

(defn ^:export init []
  (when-let [^js/Object game (:game @state)]
    (.destroy game true))
  (reset! state {:game (new Game (clj->js config))}))

(init)
