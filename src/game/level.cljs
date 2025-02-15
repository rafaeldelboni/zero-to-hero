(ns game.level
  (:require
   ["phaser" :refer [Animations]]
   [game.interop :refer [oassoc! oget]]
   [game.phaser.anims :as anims]
   [game.phaser.body :as body]
   [game.phaser.physics :as physics]
   [game.phaser.registry :as registry]))

(defn- sprite->frame-name [^js/Object sprite]
  (-> sprite .-frame .-name))

(defn- level-objects->group!
  ([^js/Object ctx ^js/Object level layer-name]
   (level-objects->group! ctx level layer-name {}))
  ([^js/Object ctx ^js/Object level layer-name group-opts]
   (let [^js/Object group (physics/add-group! ctx group-opts)
         ^js/Object objects (.createFromObjects level layer-name)]
     (.addMultiple group objects)
     (.setName group layer-name)
     group)))

(defn- set-ground!
  [^js/Object ctx ^js/Object player ^js/Object level ^js/Object tileset]
  (let [ground (.createLayer level "ground" tileset)]
    (.setCollisionByProperty ground #js {:collides true})
    (physics/add-collider! ctx player ground)
    ground))

(defn- set-pushables!
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [^js/Object boxes (level-objects->group! ctx level "pushables")]
    (doseq [^js/Object box (.-entries (.-children boxes))]
      (-> (.-body box)
          (body/set-size! 14 16)
          (body/set-slide-factor! 0.1 0.1)
          (body/set-drag! 10 10)))
    (physics/add-collider!
     ctx boxes boxes (fn [collider-1 collider-2]
                       (let [^js/Object b1 (.-body collider-1)
                             ^js/Object b2 (.-body collider-2)]
                         (if (and (body/touching-down? b1)
                                  (body/touching-up? b2))
                           (body/set-allow-gravity! b1 false)
                           (body/set-allow-gravity! b1 true)))))
    (physics/add-collider!
     ctx player boxes (fn [collider-1 collider-2]
                        (let [level (registry/get! ctx :game/level)
                              ^js/Object b1 (.-body collider-1)
                              ^js/Object b2 (.-body collider-2)]
                          (if (> level 1)
                            (if (and (body/touching-down? b1)
                                     (body/touching-up? b2))
                              (body/set-static! b2)
                              (body/set-kinetic! b2))
                            (body/set-static! b2)))))

    boxes))

(defn- set-pushable-blocks!
  [^js/Object ctx ^js/Object pushables ^js/Object level]
  (let [^js/Object blocks (level-objects->group! ctx level "pushable-blocks")]
    (physics/add-overlap!
     ctx blocks pushables (fn [_collider-1 collider-2]
                            (let [^js/Object b2 (.-body collider-2)]
                              (if (or (body/touching-right? b2)
                                      (body/touching-left? b2))
                                (body/set-static! b2)
                                (body/set-kinetic! b2)))))
    blocks))

(defn- pickup!
  [^js/Object ctx ^js/Object target target-keyword]
  (registry/inc! ctx target-keyword 1)
  (.setEnable (.-body target) false)
  (-> ctx .-tweens
      (.add #js {:targets target
                 :angle #js {:from 0 :to 360}
                 :scaleX 0
                 :scaleY 0
                 :duration 200
                 :delay 50
                 :ease "Linear"
                 :onComplete #(.destroy target)})))

(defn- update-pickups-via-registry [^js/Object _ctx ^js/Object pickables _p k v]
  (when (and (= k :game/level) (= v 3))
    (doseq [^js/Object pickup (.-entries (.-children pickables))]
      (when (.-name pickup)
        (.setAlpha pickup 1)))))

(defn- set-pickables
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [orb-name 2
        diamond-name 22
        heart-name 42
        hidden-heart-name 41
        ^js/Object pickables (level-objects->group! ctx level "pickables")]
    (registry/on-change! ctx update-pickups-via-registry pickables)
    (doseq [^js/Object pickup (.-entries (.-children pickables))]
      (condp = (sprite->frame-name pickup)
        orb-name (do (.setName pickup "orb")
                     (.play pickup "orb"))
        diamond-name (do (.setName pickup "diamond")
                         (.play pickup "diamond"))
        heart-name (do (.setName pickup "heart")
                       (.play pickup "heart"))
        hidden-heart-name (do (.setName pickup "hidden-heart")
                              (.play pickup "heart")
                              (.setAlpha pickup 0.25))))
    (physics/add-overlap!
     ctx player pickables
     (fn [^js/Object _collider-1 ^js/Object collider-2]
       (let [level (registry/get! ctx :game/level)]
         (condp = (.-name collider-2)
           "orb" (pickup! ctx collider-2 :game/level)
           "diamond" (pickup! ctx collider-2 :game/score)
           "heart" (pickup! ctx collider-2 :game/health)
           "hidden-heart" (when (> level 2) (pickup! ctx collider-2 :game/health))))))
    pickables))

(defn- set-destructibles
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [^js/Object player-attack-area (.getByName player "attack-area")
        ^js/Object destructibles (level-objects->group! ctx level "destructibles")]
    (doseq [^js/Object destructible (.-entries (.-children destructibles))]
      (-> (.-body destructible)
          body/set-static!
          (body/set-slide-factor! 0 0)))
    (physics/add-collider! ctx player destructibles)
    (physics/add-overlap!
     ctx player-attack-area destructibles
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
                          :onComplete #(.destroy collider-2)}))))))
    destructibles))

(defn- set-threats
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [hidden-spike 203
        active-spike 183
        ^js/Object threats (level-objects->group!
                            ctx level "threats" {:allowGravity false})]
    (doseq [^js/Object threat (.-entries (.-children threats))]
      (-> (.-body threat)
          (body/set-size! 16 8)
          (body/set-offset! 0 8))
      (if (= (sprite->frame-name threat) hidden-spike)
        (do
          (.play threat "trap")
          (oassoc! threat :threat/active false)
          (-> threat (.on (-> Animations .-Events .-ANIMATION_UPDATE)
                          (fn []
                            (condp = (sprite->frame-name threat)
                              hidden-spike (oassoc! threat :threat/active false)
                              active-spike (oassoc! threat :threat/active true)))
                          threat)))
        (oassoc! threat :threat/active true)))
    (physics/add-overlap!
     ctx player threats
     (fn [^js/Object collider-1 ^js/Object collider-2]
       (when (and (not (oget collider-1 :player/invulnerable))
                  (oget collider-2 :threat/active))
         (registry/inc! ctx :game/health -1))))
    threats))

(defn- create-anims! [^js/Object ctx]
  (let [generate-frames (partial anims/generate-frame-numbers ctx "monochrome-ss")]
    (anims/create! ctx {:key "trap"
                        :frames (generate-frames {:frames [203 183]})
                        :frameRate 0.5
                        :repeat -1})
    (anims/create! ctx {:key "heart"
                        :frames (generate-frames {:frames [40 41 42 41]})
                        :frameRate 5
                        :repeat -1})
    (anims/create! ctx {:key "diamond"
                        :frames (generate-frames {:frames [20 21 22 21]})
                        :frameRate 5
                        :repeat -1})
    (anims/create! ctx {:key "orb"
                        :frames (generate-frames {:frames [1 2 1]})
                        :frameRate 5
                        :repeat -1})))

(defn create-tiled-level!
  [^js/Object ctx ^js/Object player map-key]
  (create-anims! ctx)
  (let [level (-> ctx .-make (.tilemap #js {:key map-key}))
        tileset (.addTilesetImage level
                                  "monochrome"
                                  "monochrome-ss"
                                  16 16 1 2 0
                                  #js {:x 0 :y 0})
        ground (set-ground! ctx player level tileset)
        _threats (set-threats ctx player level)
        pushables (set-pushables! ctx player level)
        pushable-blocks (set-pushable-blocks! ctx pushables level)
        destructibles (set-destructibles ctx player level)
        pickables (set-pickables ctx player level)]
    (physics/add-collider! ctx pushables ground)
    (physics/add-collider! ctx pushable-blocks ground)
    (physics/add-collider! ctx destructibles ground)
    (physics/add-collider! ctx pickables ground)
    level))

(defn create-camera!
  [^js/Object ctx ^js/Object player]
  (-> ctx .-cameras .-main (.setRoundPixels false))
  (-> ctx .-cameras .-main (.startFollow player false))
  (-> ctx .-cameras .-main .-followOffset (.set 0 75)))
