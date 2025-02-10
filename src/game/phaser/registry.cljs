(ns game.phaser.registry
  (:require
   [game.interop :refer [keyword->str str->keyword]]))

(defn inc!
  [^js/Object scene k v]
  (-> scene .-registry (.inc (keyword->str k) v)))

(defn set!
  [^js/Object scene k v]
  (-> scene .-registry (.set (keyword->str k) v)))

(defn get!
  [^js/Object scene k]
  (-> scene .-registry (.get (keyword->str k))))

(defn on-change!
  [^js/Object scene
   callback-fn
   & contexts]
  (let [context-objects (into [scene] contexts)
        modded-fn (fn [p k v]
                    ((apply partial callback-fn context-objects) p (str->keyword k) v))]
    (-> scene .-registry
        (.each (fn [p k v] (modded-fn p k v))))
    (-> scene .-registry .-events
        (.on "changedata" modded-fn scene))))
