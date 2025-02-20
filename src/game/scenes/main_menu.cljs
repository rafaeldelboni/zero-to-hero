(ns game.scenes.main-menu
  (:require
   [game.interop :refer [oassoc! oget]]
   [game.phaser.cursors :as cursors]
   [game.phaser.registry :as registry]
   [game.phaser.text :as text]
   [game.time :as time]))

(defn create! []
  (this-as ^js/Object this
    (let [loading-text (text/set-text! this "loading..."
                                       {:x 150 :y 240
                                        :alpha 1
                                        :fontFamily "Courier New"
                                        :fontSize "16px"
                                        :fontStyle "bold"
                                        :fill "#cccccc"})
          start-text (text/set-text! this "press [space] to start!"
                                     {:x 100 :y 240
                                      :alpha 0
                                      :fontFamily "Courier New"
                                      :fontSize "16px"
                                      :fontStyle "bold"
                                      :fill "#cccccc"})]
      (-> this .-add (.image 200 140 "logo"))
      (-> this .-add (.image 200 60 "logo-text"))
      (oassoc! this :load/loading-text loading-text)
      (oassoc! this :load/start-text start-text)
      (registry/set! this :game/health 3)
      (registry/set! this :game/score 0)
      (registry/set! this :game/level 0)
      (registry/set! this :game/time (time/now-seconds!)))))

(defn update! []
  (this-as ^js/Object this
    (let [cursors (cursors/create! this)]
      (when (registry/get! this :load/font)
        (.setAlpha ^js/Object (oget this :load/loading-text) 0)
        (.setAlpha ^js/Object (oget this :load/start-text) 1)
        (when (cursors/jump-just-pressed? cursors)
          (-> this .-scene (.start "hud"))
          (-> this .-scene (.start "level-1")))))))
