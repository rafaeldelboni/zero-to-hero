(ns game.core
  (:require
   ["phaser" :refer [AUTO Game Scale]]
   [game.interop :refer [oassoc!]]
   [game.player :as player]))

(defonce state (atom {}))

(defn game-preload []
  (this-as ^js/Object this
    (-> this .-load (.aseprite "hero" "assets/sprites/hero.png" "assets/sprites/hero.json"))))

(defn game-create []
  (this-as ^js/Object this
    (oassoc! this :player (player/obj-create this))
    (oassoc! this :cursors (-> this .-input .-keyboard (.createCursorKeys)))))

(defn game-update []
  (this-as ^js/Object this
    (player/obj-update this)))

(def config {:type AUTO
             :parent "game"
             :width 400
             :height 300
             :pixelArt true
             :backgroundColor "#ffffff"
             :physics {:default "arcade"
                       :arcade {:gravity {:y 0}
                                :debug true}}
             :scale {:mode (.-FIT Scale)
                     :autoCenter (.-CENTER_BOTH Scale)}
             :scene [{:key "main-scene"
                      :preload game-preload
                      :create game-create
                      :update game-update}]})

(defn ^:export init []
  (when-let [^js/Object game (:game @state)]
    (.destroy game true))
  (reset! state {:game (new Game (clj->js config))}))

(init)
