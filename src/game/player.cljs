(ns game.player
  (:require
   ["phaser" :refer [Input]]
   [game.interop :refer [oassoc! oget]]))

(def suffixes ["head" "arms" "torso" "sword" "legs" "boots"])

(defn- create-animation!
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

(defn- get-key-maps [state]
  (let [default-suffixes suffixes
        sufixes (case state
                  "blob" ["torso"]
                  "attack" (merge default-suffixes "slash")
                  default-suffixes)]
    (map (fn [sufix]
           {:sufix sufix :state state :key-name (str state "-" sufix)}) sufixes)))

(defn- create-animations!
  [^js/Object ctx state animation-config]
  (let [key-maps (get-key-maps state)]
    (doseq [{:keys [key-name]} key-maps]
      (create-animation! ctx (assoc animation-config :key-name key-name)))))

(defn- play-container-animations!
  [^js/Object container ^js/String state]
  ; TODO check jumping?
  (when (not= (oget container :prev-animation) state)
    (oassoc! container :prev-animation state)
    (let [key-maps (get-key-maps state)]
      (doseq [{:keys [sufix key-name]} key-maps]
        (.play (.getByName container sufix) key-name)))))

(defn- flip-x-container-sprites!
  [^js/Object container ^js/String flip?]
  (doseq [sufix suffixes]
    (oassoc! (.getByName container sufix) :flipX flip?)))

(defn- create-all-animations! [^js/Object ctx]
  (create-animations! ctx "attack"
                      {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1})
  (create-animations! ctx "blob"
                      {:source "hero" :start 0 :end 0 :frame-rate 1 :repeat -1})
  (create-animations! ctx "idle"
                      {:source "hero" :start 0 :end 1 :frame-rate 4 :repeat -1})
  (create-animations! ctx "jump"
                      {:source "hero" :start 0 :end 1 :frame-rate 4 :repeat -1})
  (create-animations! ctx "push"
                      {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1})
  (create-animations! ctx "walk"
                      {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1}))

(defn- create-sprite! [^js/Object ctx x y source initial-sprite sprite-name]
  (doto (-> ctx .-add (.sprite x y source initial-sprite))
    (.setName sprite-name)))

(defn- create-container! [^js/Object ctx]
  (let [head (create-sprite! ctx 0 0 "hero" "blob-empty-0" "head")
        arms (create-sprite! ctx 0 0 "hero" "blob-empty-0" "arms")
        torso (create-sprite! ctx 0 0 "hero" "blob-empty-0" "torso")
        sword (create-sprite! ctx 0 0 "hero" "blob-empty-0" "sword")
        slash (create-sprite! ctx 0 0 "hero" "blob-empty-0" "slash")
        legs (create-sprite! ctx 0 0 "hero" "blob-empty-0" "legs")
        boots (create-sprite! ctx 0 0 "hero" "blob-empty-0" "boots")
        container (doto (-> ctx .-add (.container 200 150 #js [head arms torso sword slash legs boots]))
                    (.setSize 32 32))]

    (play-container-animations! container "idle")

    (-> ctx .-physics .-world (.enable container))
    (doto (.-body container)
      (.setCollideWorldBounds true))

    container))

(defn- idle [^js/Object player]
  (play-container-animations! player "idle")
  (-> player .-body (.setVelocity 0 0)))

; TODO move game toward platform to make jump works
(defn- jump [^js/Object player velocity]
  (oassoc! player :jumping true)
  (play-container-animations! player "jump")
  (.setVelocityY (.-body player) (* velocity -1)))

(defn- move [^js/Object player direction velocity]
  (let [body ^js/Object (.-body player)]
    ; TODO check jumping
    (case direction
      :left (do (.setVelocityX body (* velocity -1))
                (flip-x-container-sprites! player true))
      :right (do (.setVelocityX body velocity)
                 (flip-x-container-sprites! player false))
      :up (.setVelocityY body (* velocity -1))
      :down (.setVelocityY body velocity)))
  (play-container-animations! player "walk"))

(defn obj-create [^js/Object ctx]
  (create-all-animations! ctx)
  (create-container! ctx))

(defn obj-update [^js/Object ctx]
  (let [player (oget ctx :player)
        cursors ^js/Object (oget ctx :cursors)]
    (cond
      (-> cursors .-left .-isDown) (move player :left 150)
      (-> cursors .-right .-isDown) (move player :right 150)
      (-> cursors .-down .-isDown) (move player :down 150)
      :else (idle player))

    (cond
      ((-> Input .-Keyboard .-JustDown) (.-up cursors)) (jump player 250)
      (-> player .-body .-blocked .-down) (oassoc! player :jumping false))))
