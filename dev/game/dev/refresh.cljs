(ns game.dev.refresh
  (:require
   [game.core :refer [init]]
   [shadow.resource :as rc]))

(defn- empty-container! [id]
  (let [container (js/document.getElementById id)]
    (set! (.-innerHTML container) "")
    container))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:dev/before-load before-refresh []
  (empty-container! "game"))

; register asset to hotreload
(rc/inline "public/assets/level1.json")
(rc/inline "public/assets/sprites/hero.json")
; init game from this ns
(init)
