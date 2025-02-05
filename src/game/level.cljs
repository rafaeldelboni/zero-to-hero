(ns game.level
  (:require
   ["phaser" :refer [Animations]]
   [game.interop :refer [oassoc! oget]]))

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

(defn- set-pickables
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [^js/Object pickables (-> ctx .-physics .-add (.group #js {}))
        ^js/Object objects (.createFromObjects level "pickables")]
    (.addMultiple pickables objects)
    (doseq [^js/Object pickup (.-entries (.-children pickables))]
      (.play pickup "diamond"))
    (-> ctx .-physics .-add
        (.overlap player pickables
                  (fn [^js/Object _collider-1 ^js/Object collider-2]
                    (-> ctx .-registry (.inc "game/score" 1))
                    (.setEnable (.-body collider-2) false)
                    (-> ctx .-tweens
                        (.add #js {:targets collider-2
                                   :angle #js {:from 0 :to 360}
                                   :scaleX 0
                                   :scaleY 0
                                   :duration 200
                                   :delay 50
                                   :ease "Linear"
                                   :onComplete #(.destroy collider-2)})))))
    pickables))

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
                    (let [attacking? (oget player :player/attack)]
                      (when attacking?
                        (.setEnable (.-body collider-2) false)
                        (-> ctx .-tweens
                            (.add #js {:targets collider-2
                                       :angle #js {:from 0 :to 90}
                                       :alpha 0
                                       :scaleX 0
                                       :scaleY 0
                                       :duration 400
                                       :delay 400
                                       :ease "Linear"
                                       :onComplete #(.destroy collider-2)})))))))
    destructibles))

(defn- set-threats
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [get-name (fn [t] (-> t .-frame .-name str))
        hidden-spike "203"
        active-spike "183"
        ^js/Object threats (-> ctx .-physics .-add (.group #js {:allowGravity false}))
        ^js/Object objects (.createFromObjects level "threats")]
    (.addMultiple threats objects)
    (doseq [^js/Object threat (.-entries (.-children threats))]
      (if (= (get-name threat) hidden-spike)
        (do
          (.play threat "trap")
          (oassoc! threat :threat/active false)
          (-> threat (.on (-> Animations .-Events .-ANIMATION_UPDATE)
                          (fn []
                            (condp = (get-name threat)
                              hidden-spike (oassoc! threat :threat/active false)
                              active-spike (oassoc! threat :threat/active true)))
                          threat)))
        (oassoc! threat :threat/active true)))
    (-> ctx .-physics .-add
        (.overlap player threats
                  (fn [^js/Object collider-1 ^js/Object collider-2]
                    (when (and (not (oget collider-1 :player/invulnerable))
                               (oget collider-2 :threat/active))
                      (-> ctx .-registry (.inc "game/health" -1))))))
    threats))

(defn- create-anims! [^js/Object ctx]
  (-> ctx .-anims
      (.create (clj->js {:key "trap"
                         :frames (-> ctx .-anims
                                     (.generateFrameNumbers
                                      "monochrome-ss"
                                      (clj->js {:frames [203 183]})))
                         :frameRate 0.5
                         :repeat -1})))
  (-> ctx .-anims
      (.create (clj->js {:key "diamond"
                         :frames (-> ctx .-anims
                                     (.generateFrameNumbers
                                      "monochrome-ss"
                                      (clj->js {:frames [20 21 22 21]})))
                         :frameRate 5
                         :repeat -1}))))

(defn create-tiled-level!
  [^js/Object ctx ^js/Object player map-key]
  (create-anims! ctx)
  (let [level (-> ctx .-make (.tilemap #js {:key map-key}))
        tileset (.addTilesetImage level "monochrome" "monochrome-ss")
        pushables (set-pushables! ctx player level)
        destructibles (set-destructibles ctx player level)
        pickables (set-pickables ctx player level)
        ground (set-ground! ctx player level tileset)]
    (set-threats ctx player level)
    (-> ctx .-physics .-add (.collider pushables ground))
    (-> ctx .-physics .-add (.collider destructibles ground))
    (-> ctx .-physics .-add (.collider pickables ground))
    level))

(defn create-camera!
  [^js/Object ctx ^js/Object player]
  (-> ctx .-cameras .-main (.setRoundPixels false))
  (-> ctx .-cameras .-main (.startFollow player false))
  (-> ctx .-cameras .-main .-followOffset (.set 0 75)))
