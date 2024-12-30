(ns game.interop
  (:require
   [goog.object :as obj]))

(defn oassoc! [^js/Object coll k v]
  (let [prop (if (keyword? k) (name k) k)]
    (obj/set coll prop v)))

(defn oget
  ([^js/Object coll k]
   (oget coll k nil))
  ([^js/Object coll k not-found]
   (let [prop (if (keyword? k) (name k) k)]
     (obj/get coll prop not-found))))
