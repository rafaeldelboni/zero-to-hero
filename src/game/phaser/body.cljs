(ns game.phaser.body)

(defn set-immovable! ^js/Object
  [^js/Object body v]
  (.setImmovable body v)
  body)

(defn set-moves! ^js/Object
  [^js/Object body v]
  (set! (.-moves body) v)
  body)

(defn set-pushable! ^js/Object
  [^js/Object body v]
  (set! (.-pushable body) v)
  body)

(defn set-slide-factor! ^js/Object
  [^js/Object body x y]
  (.set (.-slideFactor body) x y)
  body)

(defn set-velocity! ^js/Object
  [^js/Object body x y]
  (.setVelocity body x y)
  body)

(defn set-velocity-x! ^js/Object
  [^js/Object body x]
  (.setVelocityX body x)
  body)

(defn set-velocity-y! ^js/Object
  [^js/Object body y]
  (.setVelocityY body y)
  body)

(defn get-velocity-y
  [^js/Object body]
  (.-y (.-velocity body)))

(defn set-bounce! ^js/Object
  [^js/Object body v]
  (.setBounce body v)
  body)

(defn set-allow-gravity! ^js/Object
  [^js/Object body v]
  (.setAllowGravity body v)
  body)

(defn touching?
  [^js/Object body]
  (-> body .-touching))

(defn touching-left?
  [^js/Object body]
  (-> body .-touching .-left))

(defn touching-right?
  [^js/Object body]
  (-> body .-touching .-right))

(defn touching-down?
  [^js/Object body]
  (-> body .-touching .-down))

(defn touching-up?
  [^js/Object body]
  (-> body .-touching .-up))

(defn blocked-left?
  [^js/Object body]
  (-> body .-blocked .-left))

(defn blocked-right?
  [^js/Object body]
  (-> body .-blocked .-right))

(defn blocked-down?
  [^js/Object body]
  (-> body .-blocked .-down))
