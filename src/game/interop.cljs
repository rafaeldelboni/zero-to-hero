(ns game.interop
  (:require
   [goog.object :as obj]))

(def debug? goog.DEBUG)

(defn- keyword->str [k]
  (if (keyword? k) (str (symbol k)) k))

(defn oassoc! [^js/Object coll k v]
  (let [prop (keyword->str k)]
    (obj/set coll prop v)))

(defn oget
  ([^js/Object coll k]
   (oget coll k nil))
  ([^js/Object coll k not-found]
   (let [prop (keyword->str k)]
     (obj/get coll prop not-found))))

(defn oupdate!
  ([^js/Object m k f]
   (oassoc! m k (f (oget m k))))
  ([^js/Object m k f x]
   (oassoc! m k (f (oget m k) x)))
  ([^js/Object m k f x y]
   (oassoc! m k (f (oget m k) x y)))
  ([^js/Object m k f x y z]
   (oassoc! m k (f (oget m k) x y z)))
  ([^js/Object m k f x y z & more]
   (oassoc! m k (apply f (oget m k) x y z more))))
