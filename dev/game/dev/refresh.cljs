(ns game.dev.refresh)

(defn- empty-container! [id]
  (let [container (js/document.getElementById id)]
    (set! (.-innerHTML container) "")
    container))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:dev/before-load before-refresh []
  (empty-container! "game"))
