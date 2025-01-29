(ns game.player
  (:require
   ["phaser" :refer [Input]]
   [game.interop :refer [oassoc! oget oupdate!]]))

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
  (let [prev-state (oget container :prev-state)]
    (when (not= prev-state state)
      (let [prev-key-maps (get-key-maps prev-state)
            key-maps (get-key-maps state)]
        (doseq [{:keys [sufix key-name]} prev-key-maps
                :let [sprite (.getByName container sufix)]]
          (.setVisible sprite false)
          (.stop sprite key-name))
        (doseq [{:keys [sufix key-name]} key-maps
                :let [sprite (.getByName container sufix)]]
          (.setVisible sprite true)
          (.play sprite key-name)))
      (oassoc! container :prev-state state))))

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
                    (.setName "player")
                    (.setSize 32 32))]

    (play-container-animations! container "idle")

    (-> ctx .-physics .-world (.enable container))
    (doto (.-body container)
      (.setBounce 0.0))

    (oassoc! container :blob false)

    container))

(defn resize-player [^js/Object player w h]
  (.setSize player w h)
  (.setSize (.-body player) w h))

(defn- collides-above? [^js/Object container ^js/Object level]
  (let [x (.-x container)
        y (- (.-y container) (.-height container))
        tile (.getTileAtWorldXY level x y)]
    (if tile
      (.-collides (.-properties tile))
      false)))

(defn- blob? [^js/Object player]
  (oget player :blob))

(defn- toggle-blob [^js/Object player ^js/Object level]
  (when-not (and (blob? player)
                 (collides-above? player level))
    (oupdate! player :blob not)
    (.setVelocity (.-body player) 0 -150)
    (if (blob? player)
      (resize-player player 32 16)
      (resize-player player 32 32))))

(defn- on-floor? [^js/Object container]
  (when-let [body (-> container .-body)]
    (or (-> body .-blocked .-down)
        (-> body .-touching .-down))))

(defn- jumping? [^js/Object player]
  (not (zero? (.-y (.-velocity (.-body player))))))

(defn- play!
  [^js/Object player ^js/String state]
  (cond
    (blob? player) (play-container-animations! player "blob")
    (jumping? player) (play-container-animations! player "jump")
    :else (play-container-animations! player state)))

(defn- idle [^js/Object player]
  (-> player .-body (.setVelocityX 0))
  (play! player "idle"))

(defn- jump [^js/Object player velocity]
  (.setVelocityY (.-body player) (* velocity -1))
  (play! player "jump"))

(defn- move [^js/Object player direction velocity]
  (let [body ^js/Object (.-body player)]
    (case direction
      :left (do (.setVelocityX body (* velocity -1))
                (flip-x-container-sprites! player true))
      :right (do (.setVelocityX body velocity)
                 (flip-x-container-sprites! player false))
      :up (.setVelocityY body (* velocity -1))
      :down (.setVelocityY body velocity)))
  (play! player "walk"))

(defn create! [^js/Object ctx]
  (create-all-animations! ctx)
  (create-container! ctx))

(defn update! [^js/Object ctx]
  (let [player (oget ctx :player)
        level (oget ctx :level)
        cursors ^js/Object (oget ctx :cursors)]
    (cond
      (-> cursors .-left .-isDown) (move player :left 150)
      (-> cursors .-right .-isDown) (move player :right 150)
      :else (idle player))

    (when ((-> Input .-Keyboard .-JustDown) (.-down cursors))
      (toggle-blob player level))

    (when (and ((-> Input .-Keyboard .-JustDown) (.-up cursors))
               (on-floor? player))
      (jump player 400))))
