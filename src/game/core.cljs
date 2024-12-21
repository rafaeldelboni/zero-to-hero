(ns game.core
  (:require
   ["phaser" :refer [AUTO Game Scale]]
   [goog.object :as obj]))

(defonce state (atom {}))

(defn game-preload []
  (this-as ^js/Object this
    (-> this .-load (.aseprite "hero" "assets/sprites/hero.png" "assets/sprites/hero.json"))))

(defn create-animation!
  [^js/Object ctx {:keys [source key-name start end frame-rate repeat]}]
  (-> ctx .-anims (.create (clj->js {:key key-name
                                     :frames (-> ctx .-anims
                                                 (.generateFrameNames
                                                  source
                                                  #js {:prefix (str key-name "-")
                                                       :start start
                                                       :end end}))
                                     :frameRate frame-rate
                                     :repeat repeat}))))

(defn get-key-maps [state]
  (let [default-suffixes ["head" "arms" "torso" "sword" "legs" "boots"]
        sufixes (case state
                  "blob" ["torso"]
                  "attack" (merge default-suffixes "slash")
                  default-suffixes)]
    (map (fn [sufix]
           {:sufix sufix :state state :key-name (str state "-" sufix)}) sufixes)))

(defn create-animations!
  [^js/Object ctx state animation-config]
  (let [key-maps (get-key-maps state)]
    (doseq [{:keys [key-name]} key-maps]
      (create-animation! ctx (assoc animation-config :key-name key-name)))))

(defn play-container-animations!
  [^js/Object container ^js/String state]
  (let [key-maps (get-key-maps state)]
    (doseq [{:keys [sufix key-name]} key-maps]
      (.play (.getByName container sufix) key-name))))

(defn player [^js/Object ctx]
  (let [head (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "head"))
        arms (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "arms"))
        torso (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "torso"))
        sword (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "sword"))
        slash (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "slash"))
        legs (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "legs"))
        boots (doto (-> ctx .-add (.sprite 0 0 "hero" "blob-empty-0")) (.setName "boots"))
        container (doto (-> ctx .-add (.container 200 150 #js [head arms torso sword slash legs boots]))
                    (.setSize 32 32))]

    (play-container-animations! container "blob")

    (-> ctx .-physics .-world (.enable container))
    (doto (.-body container)
      (.setCollideWorldBounds true))

    container))

(defn game-create []
  (this-as ^js/Object this
    (create-animations! this "attack" {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1})
    (create-animations! this "blob" {:source "hero" :start 0 :end 0 :frame-rate 1 :repeat -1})
    (create-animations! this "idle" {:source "hero" :start 0 :end 1 :frame-rate 4 :repeat -1})
    (create-animations! this "jump" {:source "hero" :start 0 :end 1 :frame-rate 4 :repeat -1})
    (create-animations! this "push" {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1})
    (create-animations! this "walk" {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1})
    (obj/set this "player" (player this))
    (obj/set this "cursors" (-> this .-input .-keyboard (.createCursorKeys)))))

(defn game-update []
  (this-as ^js/Object this
    (let [player (.-body (obj/get this "player"))
          cursors (obj/get this "cursors")]
      (-> player (.setVelocity 0 0))
      (cond
        (-> cursors .-left .-isDown) (.setVelocityX player -150)
        (-> cursors .-right .-isDown) (.setVelocityX player 150))
      (cond
        (-> cursors .-up .-isDown) (.setVelocityY player -150)
        (-> cursors .-down .-isDown) (.setVelocityY player 150)))))

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
