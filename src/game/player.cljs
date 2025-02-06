(ns game.player
  (:require
   ["phaser" :refer [Input]]
   [game.player.animations :as player.anims]
   [game.interop :refer [oassoc! oget oupdate!]]))

(defn- update-via-registry
  [^js/Object ctx
   ^js/Object player
   _p updated-key updated-value]
  (when (= updated-key "game/health")
    (let [current-health (oget player :game/health)
          damage? (> current-health updated-value)]
      (oassoc! player updated-key updated-value)

      (when (<= updated-value 0)
        ;; TODO move to game over scene
        (-> ctx .-registry (.set "game/health" 3))
        (-> ctx .-scene (.start "test-level")))

      (when (and damage? (> updated-value 0) (< updated-value 3))
        (oassoc! player :player/invulnerable true)
        (-> ctx .-tweens
            (.add #js {:targets player
                       :alpha 0.35
                       :yoyo true
                       :duration 1000
                       :ease "Bounce"
                       :onComplete #(oassoc! player :player/invulnerable false)}))))))

(defn- create-container! [^js/Object ctx]
  (let [head (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "head")
        arms (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "arms")
        torso (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "torso")
        sword (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "sword")
        slash (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "slash")
        legs (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "legs")
        boots (player.anims/create-sprite! ctx 0 0 "hero" "blob-empty-0" "boots")
        attack-area (doto (-> ctx .-add (.container 0 0 #js []))
                      (.setName "attack-area")
                      (.setSize 32 32))
        container (doto (-> ctx .-add (.container 200 150 #js [head arms torso sword slash legs boots attack-area]))
                    (.setName "player")
                    (.setSize 16 32))]

    (.setDepth container 1)

    (-> ctx .-physics .-world (.enable attack-area))
    (doto (.-body attack-area)
      (.setAllowGravity false))

    (-> ctx .-physics .-world (.enable container))
    (doto (.-body container)
      (.setBounce 0.0))

    (.on (.getByName container "slash")
         "animationcomplete-attack-slash"
         (fn [] (oassoc! container :player/attack false)))

    (oassoc! container :player/invulnerable false)
    (oassoc! container :player/blob false)
    (oassoc! container :player/attack false)
    (player.anims/play-container-animations! container "idle")

    (-> ctx .-registry
        (.each (fn [p k v] (update-via-registry ctx container p k v))))
    (-> ctx .-registry .-events
        (.on "changedata" (partial update-via-registry ctx container) ctx))

    container))

(defn- collides-above? [^js/Object container ^js/Object level]
  (let [x (.-x container)
        y (- (.-y container) (.-height container))
        tile (.getTileAtWorldXY level x y)]
    (if tile
      (.-collides (.-properties tile))
      false)))

(defn- blob? [^js/Object player]
  (oget player :player/blob))

(defn- on-floor? [^js/Object container]
  (when-let [body (-> container .-body)]
    (or (-> body .-blocked .-down)
        (-> body .-touching .-down))))

(defn- pushing? [^js/Object container]
  (when-let [body (-> container .-body)]
    (or (-> body .-blocked .-left)
        (-> body .-touching .-left)
        (-> body .-blocked .-right)
        (-> body .-touching .-right))))

(defn- jumping? [^js/Object player]
  (not (zero? (.-y (.-velocity (.-body player))))))

(defn- attacking? [^js/Object player]
  (oget player :player/attack))

(defn- play!
  [^js/Object player ^js/String state]
  (cond
    (blob? player) (player.anims/play-container-animations! player "blob")
    (pushing? player) (player.anims/play-container-animations! player "push")
    (jumping? player) (player.anims/play-container-animations! player "jump")
    :else (player.anims/play-container-animations! player state)))

(defn- resize-player [^js/Object player w h]
  (.setSize player w h)
  (.setSize (.-body player) w h))

(defn- toggle-blob [^js/Object player ^js/Object level]
  (when-not (and (blob? player)
                 (collides-above? player level))
    (oupdate! player :player/blob not)
    (.setVelocity (.-body player) 0 -150)
    (if (blob? player)
      (resize-player player 16 16)
      (resize-player player 16 32))))

(defn- idle [^js/Object player]
  (-> player .-body (.setVelocityX 0))
  (play! player "idle"))

(defn- jump [^js/Object player velocity]
  (.setVelocityY (.-body player) (* velocity -1))
  (play! player "jump"))

(defn- attack [^js/Object player]
  (oassoc! player :player/attack true)
  (.setVelocityX (.-body player) 0)
  (play! player "attack"))

(defn- move [^js/Object player direction velocity]
  (let [body ^js/Object (.-body player)]
    (case direction
      :left (do (.setVelocityX body (* velocity -1))
                (player.anims/flip-x-container-sprites! player true))
      :right (do (.setVelocityX body velocity)
                 (player.anims/flip-x-container-sprites! player false))
      :up (.setVelocityY body (* velocity -1))
      :down (.setVelocityY body velocity)))
  (play! player "walk"))

(defn create! [^js/Object ctx]
  (player.anims/create-all-animations! ctx)
  (create-container! ctx))

(defn update! [^js/Object ctx]
  (let [^js/Object cursors (oget ctx :level/cursors)
        ^js/Object player (oget ctx :level/player)
        ^js/Object level (oget ctx :level/current)]

    (when (not (attacking? player))
      (cond
        (-> cursors .-left .-isDown) (move player :left 150)
        (-> cursors .-right .-isDown) (move player :right 150)
        :else (idle player)))

    (when (and ((-> Input .-Keyboard .-JustDown) (.-space cursors))
               (not (attacking? player))
               (not (pushing? player))
               (not (jumping? player))
               (not (blob? player)))
      (attack player))

    (when (and ((-> Input .-Keyboard .-JustDown) (.-down cursors))
               (not (attacking? player)))
      (toggle-blob player level))

    (when (and ((-> Input .-Keyboard .-JustDown) (.-up cursors))
               (not (attacking? player))
               (on-floor? player))
      (jump player 400))))
