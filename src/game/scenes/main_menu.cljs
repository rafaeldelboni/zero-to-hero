(ns game.scenes.main-menu
  (:require
   [game.interop :refer [debug? oassoc! oget]]
   [game.phaser.cursors :as cursors]
   [game.phaser.registry :as registry]
   [game.phaser.text :as text]
   [game.time :as time]))

(defn create! []
  (this-as ^js/Object this
    (-> this .-add (.image 200 230 "logo"))
    (let [logo-text-img (-> this .-add (.image 200 60 "logo-text"))
          loading-text (text/set-text! this "loading..."
                                       {:x 155 :y 115
                                        :alpha 1
                                        :fontFamily "Courier New"
                                        :fontSize "16px"
                                        :fontStyle "bold"
                                        :fill "#cccccc"})
          start-text (text/set-text! this "press [space] to start!"
                                     {:x 95 :y 115
                                      :alpha 0
                                      :fontFamily "Courier New"
                                      :fontSize "16px"
                                      :fontStyle "bold"
                                      :fill "#cccccc"})]
      (.setScale logo-text-img 1.35)
      (oassoc! this :load/loading-text loading-text)
      (oassoc! this :load/start-text start-text)
      (oassoc! this :load/start-text start-text)
      (registry/set! this :game/health 3)
      (registry/set! this :game/score 0)
      (registry/set! this :game/level 0)
      (registry/set! this :game/time (time/now-seconds!)))))

(defn- start-scenes! [^js/Object this]
  (-> this .-scene .stop)
  (-> this .-scene (.start "hud"))
  (-> this .-scene (.start "level-1")))

(defn update! []
  (this-as ^js/Object this
    (if debug?
      (start-scenes! this)
      (let [cursors (cursors/create! this)]
        (when (registry/get! this :load/font)
          (.setAlpha ^js/Object (oget this :load/loading-text) 0)
          (.setAlpha ^js/Object (oget this :load/start-text) 1)
          (when (cursors/jump-just-pressed? cursors)
            (start-scenes! this)))))))
