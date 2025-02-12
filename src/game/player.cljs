(ns game.player
  (:require
   [game.interop :refer [oassoc! oget oupdate!]]
   [game.phaser.body :as body]
   [game.phaser.cursors :as cursors]
   [game.phaser.physics :as physics]
   [game.phaser.registry :as registry]
   [game.player.animations :as player.anims]))

(def blob-size {:x 12 :y 16})
(def human-size {:x 14 :y 32})

(defn- game-over! [^js/Object ctx]
  ;; TODO move to game over scene
  (registry/remove-all-listeners! ctx)
  (registry/set! ctx :game/health 3)
  (-> ctx .-scene (.start "preload")))

(defn- update-via-registry
  [^js/Object ctx
   ^js/Object player
   _p updated-key updated-value]
  ; Level up
  (when (= updated-key :game/level)
    (oassoc! player :game/level updated-value)
    (prn :game/level (oget player :game/level)))
  ; Health Change
  (when (= updated-key :game/health)
    (let [current-health (oget player :game/health)
          damage? (> current-health updated-value)]
      (oassoc! player :game/health updated-value)

      (when (<= updated-value 0)
        (game-over! ctx))

      (when (and damage? (> updated-value 0) (< updated-value 3))
        (oassoc! player :player/invulnerable true)
        (-> ctx .-tweens
            (.add #js {:targets player
                       :alpha 0.35
                       :yoyo true
                       :duration 1000
                       :ease "Bounce"
                       :onComplete #(oassoc! player :player/invulnerable false)}))))))

(defn- create-container! [^js/Object ctx x y]
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
        container (doto (-> ctx .-add (.container x y #js [head arms torso sword slash legs boots attack-area]))
                    (.setName "player")
                    (.setSize (:x blob-size) (:y blob-size)))]

    (.setDepth container 1)

    (physics/world-enable! ctx attack-area)
    (body/set-allow-gravity! (.-body attack-area) false)

    (physics/world-enable! ctx container)
    (body/set-bounce! (.-body container) 0.0)

    (.on (.getByName container "slash")
         "animationcomplete-attack-slash"
         (fn [] (oassoc! container :player/attack false)))

    (oassoc! container :player/blob true)
    (oassoc! container :player/attack false)
    (oassoc! container :player/invulnerable false)
    (player.anims/play-container-animations! container "idle" 0)

    (registry/on-change! ctx update-via-registry container)

    container))

(defn- collides-above? [^js/Object container ^js/Object level]
  (let [x (.-x container)
        y (- (.-y container) (.-height container))
        tile (.getTileAtWorldXY level x y)]
    (if tile
      (.-collides (.-properties tile))
      false)))

(defn- invulnerable? [^js/Object player]
  (oget player :player/invulnerable))

(defn- blob? [^js/Object player]
  (oget player :player/blob))

(defn- on-floor? [^js/Object container]
  (when-let [body (.-body container)]
    (or (body/blocked-down? body)
        (body/touching-down? body))))

(defn- pushing? [^js/Object container]
  (when-let [body (.-body container)]
    (or (body/blocked-left? body)
        (body/touching-left? body)
        (body/blocked-right? body)
        (body/touching-right? body))))

(defn- jumping? [^js/Object player]
  (not (zero? (body/get-velocity-y (.-body player)))))

(defn- attacking? [^js/Object player]
  (oget player :player/attack))

(defn- play!
  [^js/Object player ^js/String state]
  (let [level (oget player :game/level)]
    (cond
      (blob? player) (player.anims/play-container-animations! player "blob" level)
      (pushing? player) (player.anims/play-container-animations! player "push" level)
      (jumping? player) (player.anims/play-container-animations! player "jump" level)
      :else (player.anims/play-container-animations! player state level))))

(defn- resize-player [^js/Object player w h]
  (.setSize player w h)
  (.setSize (.-body player) w h))

(defn- toggle-blob [^js/Object player ^js/Object level]
  (when-not (and (blob? player)
                 (collides-above? player level))
    (oupdate! player :player/blob not)
    (body/set-velocity! (.-body player) 0 -150)
    (if (blob? player)
      (resize-player player (:x blob-size) (:y blob-size))
      (resize-player player (:x human-size) (:y human-size)))))

(defn- idle [^js/Object player]
  (body/set-velocity-x! (.-body player) 0)
  (play! player "idle"))

(defn- jump [^js/Object player velocity]
  (body/set-velocity-y! (.-body player) (* velocity -1))
  (play! player "jump"))

(defn- attack [^js/Object player]
  (oassoc! player :player/attack true)
  (body/set-velocity-x! (.-body player) 0)
  (play! player "attack"))

(defn- move [^js/Object player direction velocity]
  (let [body ^js/Object (.-body player)]
    (case direction
      :left (do (body/set-velocity-x! body (* velocity -1))
                (player.anims/flip-x-container-sprites! player true))
      :right (do (body/set-velocity-x! body velocity)
                 (player.anims/flip-x-container-sprites! player false))
      :up (body/set-velocity-y! body (* velocity -1))
      :down (body/set-velocity-y! body velocity)))
  (play! player "walk"))

(defn- level> [^js/Object player level]
  (> (oget player :game/level) level))

(defn- can-move? [^js/Object player]
  (not (attacking? player)))

(defn- can-blob? [^js/Object player]
  (and (level> player 0)
       (not (attacking? player))))

(defn- can-attack? [^js/Object player]
  (and (level> player 3)
       (not (attacking? player))
       (not (pushing? player))
       (not (jumping? player))
       (not (blob? player))))

(defn- can-jump? [^js/Object player]
  (and (not (attacking? player))
       (on-floor? player)))

(defn- get-speed [^js/Object player]
  (cond
    (invulnerable? player) 50
    (pushing? player) 50
    (blob? player) 150
    :else 200))

(defn- get-jump-force [^js/Object player]
  (cond
    (invulnerable? player) 200
    (blob? player) 400
    :else 450))

(defn create! [^js/Object ctx x y]
  (player.anims/create-all-animations! ctx)
  (create-container! ctx x y))

(defn update! [^js/Object ctx]
  (let [^js/Object cursor (oget ctx :level/cursors)
        ^js/Object player (oget ctx :level/player)
        ^js/Object level (oget ctx :level/current)
        speed (get-speed player)
        jump-force (get-jump-force player)]

    (when (can-move? player)
      (cond
        (cursors/left-is-pressed? cursor) (move player :left speed)
        (cursors/right-is-pressed? cursor) (move player :right speed)
        :else (idle player)))

    (when (and (cursors/attack-just-pressed? cursor)
               (can-attack? player))
      (attack player))

    (when (and (cursors/down-just-pressed? cursor)
               (can-blob? player))
      (toggle-blob player level))

    (when (and (cursors/up-just-pressed? cursor)
               (can-jump? player))
      (jump player jump-force))))
