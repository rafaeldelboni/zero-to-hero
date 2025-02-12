(ns game.player.animations
  (:require
   [game.interop :refer [oassoc! oget]]
   [game.phaser.anims :as anims]))

(def all-suffixes ["head" "arms" "torso" "sword" "legs" "boots" "slash"])

(defn level-suffixes [level]
  (cond
    (= level 0) ["torso"]
    (= level 1) ["torso" "legs"]
    (= level 2) ["torso" "legs" "arms"]
    (= level 3) ["torso" "legs" "arms" "head"]
    (= level 4) ["torso" "legs" "arms" "head" "sword" "slash"]
    :else all-suffixes))

(defn- create-animation!
  [^js/Object ctx {:keys [source key-name start end frame-rate repeat]}]
  (anims/create! ctx
                 {:key key-name
                  :frames (anims/generate-frame-names
                           ctx source {:prefix (str key-name "-")
                                       :start start
                                       :end end})
                  :frameRate frame-rate
                  :repeat repeat}))

(defn- get-state-sufixes
  ([state]
   (get-state-sufixes state nil))
  ([state level]
   (let [suffixes (if level (level-suffixes level) all-suffixes)
         base-suffixes (remove #(= "slash" %) suffixes)]
     (case state
       "blob" ["torso"]
       "attack" all-suffixes
       base-suffixes))))

(defn- sufixes->key-maps [state sufixes]
  (map (fn [sufix]
         {:sufix sufix :state state :key-name (str state "-" sufix)})
       sufixes))

(defn- get-key-maps [state]
  (sufixes->key-maps state (get-state-sufixes state)))

(defn- get-key-maps-by-level [state level]
  (sufixes->key-maps state (get-state-sufixes state level)))

(defn- create-animations!
  [^js/Object ctx state animation-config]
  (let [key-maps (get-key-maps state)]
    (doseq [{:keys [key-name]} key-maps]
      (create-animation! ctx (assoc animation-config :key-name key-name)))))

(defn play-container-animations!
  [^js/Object container ^js/String state level]
  (let [prev-state (oget container :player/prev-state)]
    (when (not= prev-state state)
      (let [prev-key-maps (get-key-maps-by-level prev-state level)
            key-maps (get-key-maps-by-level state level)]
        (doseq [{:keys [sufix key-name]} prev-key-maps
                :let [sprite (.getByName container sufix)]]
          (.setVisible sprite false)
          (.stop sprite key-name))
        (doseq [{:keys [sufix key-name]} key-maps
                :let [sprite (.getByName container sufix)]]
          (.setVisible sprite true)
          (.play sprite key-name)))
      (oassoc! container :player/prev-state state))))

(defn flip-x-container-sprites!
  [^js/Object container ^js/String flip?]
  (doseq [sufix all-suffixes]
    (oassoc! (.getByName container sufix) :flipX flip?)))

(defn create-all-animations! [^js/Object ctx]
  (create-animations! ctx "attack"
                      {:source "hero" :start 0 :end 5 :frame-rate 10 :repeat 0})
  (create-animations! ctx "blob"
                      {:source "hero" :start 0 :end 0 :frame-rate 1 :repeat -1})
  (create-animations! ctx "idle"
                      {:source "hero" :start 0 :end 1 :frame-rate 4 :repeat -1})
  (create-animations! ctx "jump"
                      {:source "hero" :start 0 :end 1 :frame-rate 4 :repeat -1})
  (create-animations! ctx "push"
                      {:source "hero" :start 0 :end 3 :frame-rate 6 :repeat -1})
  (create-animations! ctx "walk"
                      {:source "hero" :start 0 :end 3 :frame-rate 10 :repeat -1}))

(defn create-sprite! [^js/Object ctx x y source initial-sprite sprite-name]
  (doto (-> ctx .-add (.sprite x y source initial-sprite))
    (.setName sprite-name)))
