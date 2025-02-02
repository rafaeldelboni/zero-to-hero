(ns game.level
  (:require
   [game.interop :refer [oget]]))

(defn- set-ground!
  [^js/Object ctx ^js/Object player ^js/Object level ^js/Object tileset]
  (let [ground (.createLayer level "ground" tileset)]
    (.setCollisionByProperty ground #js {:collides true})
    (-> ctx .-physics .-add (.collider player ground))
    ground))

(defn- set-pushables!
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [boxes (-> ctx .-physics .-add (.group #js {}))
        objects (.createFromObjects level "pushables")]
    (.addMultiple boxes objects)
    (doseq [^js/Object box (.-entries (.-children boxes))]
      (-> box .-body .-slideFactor (.set 0 0)))
    (-> ctx .-physics .-add
        (.collider boxes boxes
                   (fn [collider-1 collider-2]
                     (let [^js/Object b1 (.-body collider-1)
                           ^js/Object b2 (.-body collider-2)]
                       (if (and (-> b1 .-touching)
                                (-> b2 .-touching))
                         (set! (.-pushable b2) false)
                         (set! (.-pushable b2) true))))))
    (-> ctx .-physics .-add
        (.collider player boxes
                   (fn [collider-1 collider-2]
                     (let [^js/Object b1 (.-body collider-1)
                           ^js/Object b2 (.-body collider-2)]
                       (if (and (-> b1 .-touching .-down)
                                (-> b2 .-touching .-up))
                         (do (.setImmovable b2 true)
                             (set! (.-moves b2) false)
                             (set! (.-pushable b2) false))
                         (do (.setImmovable b2 false)
                             (set! (.-moves b2) true)
                             (set! (.-pushable b2) true)))))))
    boxes))

(defn- set-destructibles
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [player-attack-area (.getByName player "attack-area")
        destructibles (-> ctx .-physics .-add (.group #js {}))
        objects (.createFromObjects level "destructibles")]
    (.addMultiple destructibles objects)
    (doseq [^js/Object destructible (.-entries (.-children destructibles))]
      (let [obj-body (.-body destructible)]
        (.setImmovable obj-body true)
        (set! (.-moves obj-body) false)
        (set! (.-pushable obj-body) false)
        (.set (.-slideFactor obj-body) 0 0)))
    (-> ctx .-physics .-add (.collider player destructibles))
    (-> ctx .-physics .-add
        (.overlap player-attack-area destructibles
                  (fn [^js/Object _collider-1 ^js/Object collider-2]
                    (let [attacking? (oget player :attack)]
                      (when attacking?
                        (-> ctx .-tweens
                            (.add #js {:targets collider-2
                                       :alpha 0,
                                       :duration 300,
                                       :delay 500
                                       :ease "bounce.out"
                                       :onComplete #(.destroy collider-2)})))))))

    destructibles))

(defn create-tiled-level!
  [^js/Object ctx ^js/Object player map-key]
  (let [level (-> ctx .-make (.tilemap #js {:key map-key}))
        tileset (.addTilesetImage level "monochrome" "monochrome-ss")
        pushables (set-pushables! ctx player level)
        destructibles (set-destructibles ctx player level)
        ground (set-ground! ctx player level tileset)]
    (-> ctx .-physics .-add (.collider pushables ground))
    (-> ctx .-physics .-add (.collider destructibles ground))
    level))

(defn create-camera!
  [^js/Object ctx ^js/Object player]
  (-> ctx .-cameras .-main (.setRoundPixels false))
  (-> ctx .-cameras .-main (.startFollow player false))
  (-> ctx .-cameras .-main .-followOffset (.set 0 50)))
