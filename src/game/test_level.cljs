(ns game.test-level
  (:require
   [game.interop :refer [oassoc!]]
   [game.player :as player]))

(defn preload! []
  (this-as ^js/Object this
    (-> this .-load
      (.tilemapTiledJSON "level1" "assets/level1.json"))))

(defn- set-ground!
  [^js/Object ctx ^js/Object player ^js/Object level ^js/Object tileset]
  (let [ground (.createLayer level "ground" tileset)]
    (.setCollisionByProperty ground #js {:collides true})
    (-> ctx .-physics .-add (.collider player ground))
    ground))

(defn- set-pushables!
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [boxes (-> ctx .-physics .-add (.group #js {}))
        push (.createFromObjects level "pushables")]
    (.addMultiple boxes push)
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
        objects (-> ctx .-physics .-add (.group #js {}))
        destructibles (.createFromObjects level "destructibles")]
    (.addMultiple objects destructibles)
    (doseq [^js/Object object (.-entries (.-children objects))]
      (let [obj-body (.-body object)]
        (.setImmovable obj-body true)
        (set! (.-moves obj-body) false)
        (set! (.-pushable obj-body) false)
        (.-slideFactor obj-body (.set 0 0))))
    (-> ctx .-physics .-add
        (.overlap player-attack-area objects
                  (fn [_collider-1 collider-2]
                    (prn :obj-in-attack-range)
                    (js/console.log collider-2))))
    objects))

(defn create! []
  (this-as ^js/Object this
    (let [player (player/create! this)
          cursors (-> this .-input .-keyboard (.createCursorKeys))
          level (-> this .-make (.tilemap #js {:key "level1"}))
          tileset (.addTilesetImage level "monochrome" "monochrome-ss")
          pushables (set-pushables! this player level)
          destructibles (set-destructibles this player level)
          ground (set-ground! this player level tileset)]
      (-> this .-physics .-add (.collider pushables ground))
      (-> this .-physics .-add (.collider destructibles ground))
      (oassoc! this :player player)
      (oassoc! this :cursors cursors)
      (oassoc! this :level level))))

(defn update! []
  (this-as ^js/Object this
    (player/update! this)))
