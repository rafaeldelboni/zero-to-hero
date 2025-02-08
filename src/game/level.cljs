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

(defn- set-ground!
  [^js/Object ctx ^js/Object player ^js/Object level ^js/Object tileset]
  (let [ground (.createLayer level "ground" tileset)]
    (.setCollisionByProperty ground #js {:collides true})
    (physics/add-collider! ctx player ground)
    ground))

(defn- set-pushables!
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [^js/Object boxes (physics/add-group! ctx)
        objects (.createFromObjects level "pushables")]
    (.addMultiple boxes objects)
    (doseq [^js/Object box (.-entries (.-children boxes))]
      (body/set-slide-factor! (.-body box) 0 0))
    (physics/add-collider!
     ctx boxes boxes (fn [collider-1 collider-2]
                       (let [^js/Object b1 (.-body collider-1)
                             ^js/Object b2 (.-body collider-2)]
                         (if (and (body/touching? b1)
                                  (body/touching? b2))
                           (body/set-pushable! b2 false)
                           (body/set-pushable! b2 true)))))
    (physics/add-collider!
     ctx player boxes (fn [collider-1 collider-2]
                        (let [^js/Object b1 (.-body collider-1)
                              ^js/Object b2 (.-body collider-2)]
                          (if (and (body/touching-down? b1)
                                   (body/touching-up? b2))
                            (-> b2
                                (body/set-immovable! true)
                                (body/set-moves! false)
                                (body/set-pushable! false))
                            (-> b2
                                (body/set-immovable! false)
                                (body/set-moves! true)
                                (body/set-pushable! true))))))

    boxes))

(defn- set-pickables
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [orb-name 2
        diamond-name 22
        heart-name 42
        ^js/Object pickables (physics/add-group! ctx)
        ^js/Object objects (.createFromObjects level "pickables")]
    (.addMultiple pickables objects)
    (doseq [^js/Object pickup (.-entries (.-children pickables))]
      (condp = (sprite->frame-name pickup)
        orb-name (do (.setName pickup "orb")
                     (.play pickup "orb"))
        diamond-name (do (.setName pickup "diamond")
                         (.play pickup "diamond"))
        heart-name (do (.setName pickup "heart")
                       (.play pickup "heart"))))
    (physics/add-overlap!
     ctx player pickables
     (fn [^js/Object _collider-1 ^js/Object collider-2]
       (condp = (.-name collider-2)
         "orb" (registry/inc!  ctx :game/level 1)
         "diamond" (registry/inc! ctx :game/score 1)
         "heart" (registry/inc! ctx :game/health 1))
       (.setEnable (.-body collider-2) false)
       (-> ctx .-tweens
           (.add #js {:targets collider-2
                      :angle #js {:from 0 :to 360}
                      :scaleX 0
                      :scaleY 0
                      :duration 200
                      :delay 50
                      :ease "Linear"
                      :onComplete #(.destroy collider-2)}))))
    pickables))

(defn- set-destructibles
  [^js/Object ctx ^js/Object player ^js/Object level]
  (let [^js/Object player-attack-area (.getByName player "attack-area")
        ^js/Object destructibles (physics/add-group! ctx)
        objects (.createFromObjects level "destructibles")]
    (.addMultiple destructibles objects)
    (doseq [^js/Object destructible (.-entries (.-children destructibles))]
      (-> (.-body destructible)
          (body/set-immovable! true)
          (body/set-moves! false)
          (body/set-pushable! true)
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
        ^js/Object threats (physics/add-group! ctx {:allowGravity false})
        ^js/Object objects (.createFromObjects level "threats")]
    (.addMultiple threats objects)
    (doseq [^js/Object threat (.-entries (.-children threats))]
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
        tileset (.addTilesetImage level "monochrome" "monochrome-ss")
        pushables (set-pushables! ctx player level)
        destructibles (set-destructibles ctx player level)
        pickables (set-pickables ctx player level)
        ground (set-ground! ctx player level tileset)]
    (set-threats ctx player level)
    (physics/add-collider! ctx pushables ground)
    (physics/add-collider! ctx destructibles ground)
    (physics/add-collider! ctx pickables ground)
    level))

(defn create-camera!
  [^js/Object ctx ^js/Object player]
  (-> ctx .-cameras .-main (.setRoundPixels false))
  (-> ctx .-cameras .-main (.startFollow player false))
  (-> ctx .-cameras .-main .-followOffset (.set 0 75)))
