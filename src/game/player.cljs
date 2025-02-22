(ns game.player
  (:require
   [game.interop :refer [oassoc! oget oupdate!]]
   [game.phaser.audio :as audio]
   [game.phaser.body :as body]
   [game.phaser.cursors :as cursors]
   [game.phaser.physics :as physics]
   [game.phaser.registry :as registry]
   [game.player.animations :as player.anims]))

(def blob-size {:x 12 :y 10 :offset {:x 0 :y 2}})
(def human-size {:x 14 :y 24 :offset {:x 0 :y 2}})

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

(defn- game-over! [^js/Object ctx]
  (registry/remove-all-listeners! ctx)
  (registry/set! ctx :game/health 3)
  (-> ctx .-scene .stop)
  (-> ctx .-scene (.stop "hud"))
  (-> ctx .-scene (.start "main-menu")))

(defn- resize-player [^js/Object player {:keys [x y offset]}]
  (.setSize player x y)
  (-> (.-body player)
      (body/set-size! x y)
      (body/set-offset! (:x offset) (:y offset))))

(defn- update-via-registry
  [^js/Object ctx
   ^js/Object player
   _p updated-key updated-value]
  ; Level up
  (when (= updated-key :game/level)
    (oassoc! player :game/level updated-value))
  ; Health Change
  (when (= updated-key :game/health)
    (let [current-health (oget player :game/health)
          is-damage? (> current-health updated-value)]
      (when (not is-damage?)
        (oassoc! player :game/health updated-value))

      (when (<= updated-value 0)
        (audio/key-play! player :player.audio/game-over)
        (game-over! ctx))

      (when (and is-damage? (not (invulnerable? player)) (> updated-value 0))
        (oassoc! player :game/health updated-value)
        (oassoc! player :player/invulnerable true)
        (audio/key-play! player :player.audio/damage)
        (-> ctx .-cameras .-main (.shake 100 0.01))
        (-> ctx .-tweens
            (.add #js {:targets player
                       :alpha 0.35
                       :yoyo true
                       :duration 1000
                       :ease "Bounce"
                       :onComplete #(oassoc! player :player/invulnerable false)}))))))

(defn- create-audios! [^js/Object ctx ^js/Object player]
  (oassoc! player :player.audio/attack (audio/add! ctx "attack"))
  (oassoc! player :player.audio/coin (audio/add! ctx "coin"))
  (oassoc! player :player.audio/damage (audio/add! ctx "damage"))
  (oassoc! player :player.audio/destroy (audio/add! ctx "destroy"))
  (oassoc! player :player.audio/game-over (audio/add! ctx "game-over"))
  (oassoc! player :player.audio/health (audio/add! ctx "health"))
  (oassoc! player :player.audio/jump (audio/add! ctx "jump" {:volume 0.25}))
  (oassoc! player :player.audio/move (audio/add! ctx "move" {:volume 0.05 :detune -250}))
  (oassoc! player :player.audio/orb (audio/add! ctx "orb"))
  (oassoc! player :player.audio/toggle (audio/add! ctx "toggle")))

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
    (resize-player container blob-size)
    (-> (.-body container)
        (body/set-slide-factor! 1 1)
        (body/set-drag! 0 1))

    (.on (.getByName container "slash")
         "animationcomplete-attack-slash"
         (fn [] (oassoc! container :player/attack false)))

    (oassoc! container :player/blob true)
    (oassoc! container :player/attack false)
    (oassoc! container :player/invulnerable false)
    (create-audios! ctx container)
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

(defn- play!
  [^js/Object player ^js/String state]
  (let [level (oget player :game/level)]
    (cond
      (blob? player) (player.anims/play-container-animations! player "blob" level)
      (pushing? player) (player.anims/play-container-animations! player "push" level)
      (jumping? player) (player.anims/play-container-animations! player "jump" level)
      :else (player.anims/play-container-animations! player state level))))

(defn- toggle-blob [^js/Object player ^js/Object level]
  (when-not (and (blob? player)
                 (collides-above? player level))
    (oupdate! player :player/blob not)
    (audio/key-play! player :player.audio/toggle)
    (body/set-velocity! (.-body player) 0 -150)
    (if (blob? player)
      (resize-player player blob-size)
      (resize-player player human-size))))

(defn- idle [^js/Object player]
  (body/set-velocity-x! (.-body player) 0)
  (play! player "idle"))

(defn- jump [^js/Object player velocity]
  (body/set-velocity-y! (.-body player) (* velocity -1))
  (audio/key-play! player :player.audio/jump)
  (play! player "jump"))

(defn- attack [^js/Object player]
  (oassoc! player :player/attack true)
  (body/set-velocity-x! (.-body player) 0)
  (audio/key-play! player :player.audio/attack)
  (play! player "attack"))

(defn- move [^js/Object player direction velocity]
  (let [body ^js/Object (.-body player)]
    (case direction
      :left (do (body/set-velocity-x! body (* velocity -1))
                (player.anims/flip-x-container-sprites! player true))
      :right (do (body/set-velocity-x! body velocity)
                 (player.anims/flip-x-container-sprites! player false))))
  (when (not (jumping? player))
    (audio/key-play! player :player.audio/move true))
  (play! player "walk"))

(defn- level> [^js/Object player level]
  (> (oget player :game/level) level))

(defn- can-move? [^js/Object player]
  (not (attacking? player)))

(defn- can-blob? [^js/Object player]
  (and (level> player 0)
       (not (jumping? player))
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
    (blob? player) 100
    :else 115))

(defn- get-jump-force [^js/Object player]
  (cond
    (blob? player) 275
    :else 325))

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

    (when (and (cursors/jump-just-pressed? cursor)
               (can-jump? player))
      (jump player jump-force))))
