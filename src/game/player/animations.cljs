(ns game.player.animations
  (:require
   [game.interop :refer [oassoc! oget]]))

(def all-suffixes ["head" "arms" "torso" "sword" "legs" "boots" "slash"])

(defn- create-animation!
  [^js/Object ctx {:keys [source key-name start end frame-rate repeat]}]
  (-> ctx .-anims (.create (clj->js {:key key-name
                                     :frames (-> ctx .-anims
                                                 (.generateFrameNames
                                                  source
                                                  #js {:prefix (str key-name "-")
                                                       :start start
                                                       :end end}))
                                     :frameRate frame-rate
                                     :repeat repeat}))))

(defn- get-state-sufixes [state]
  (let [base-suffixes (remove #(= "slash" %) all-suffixes)]
    (case state
      "blob" ["torso"]
      "attack" all-suffixes
      base-suffixes)))

(defn- get-key-maps [state]
  (let [sufixes (get-state-sufixes state)]
    (map (fn [sufix]
           {:sufix sufix :state state :key-name (str state "-" sufix)}) sufixes)))

(defn- create-animations!
  [^js/Object ctx state animation-config]
  (let [key-maps (get-key-maps state)]
    (doseq [{:keys [key-name]} key-maps]
      (create-animation! ctx (assoc animation-config :key-name key-name)))))

(defn play-container-animations!
  [^js/Object container ^js/String state]
  (let [prev-state (oget container :player/prev-state)]
    (when (not= prev-state state)
      (let [prev-key-maps (get-key-maps prev-state)
            key-maps (get-key-maps state)]
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
                      {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1})
  (create-animations! ctx "walk"
                      {:source "hero" :start 0 :end 3 :frame-rate 8 :repeat -1}))

(defn create-sprite! [^js/Object ctx x y source initial-sprite sprite-name]
  (doto (-> ctx .-add (.sprite x y source initial-sprite))
    (.setName sprite-name)))
