(ns game.phaser.physics)

(defn add-group!
  (^js/Object [^js/Object scene]
   (add-group! scene {}))
  (^js/Object [^js/Object scene opts]
   (-> scene .-physics .-add (.group (clj->js opts)))))

(defn add-collider! ^js/Object
  (^js/Object [^js/Object scene ^js/Object group-1 ^js/Object group-2]
   (-> scene .-physics .-add (.collider group-1 group-2)))
  (^js/Object [^js/Object scene ^js/Object group-1 ^js/Object group-2 collide-callback]
   (-> scene .-physics .-add (.collider group-1 group-2 collide-callback)))
  (^js/Object [^js/Object scene ^js/Object group-1 ^js/Object group-2 collide-callback process-callback]
   (-> scene .-physics .-add (.collider group-1 group-2 collide-callback process-callback))))

(defn add-overlap!
  (^js/Object [^js/Object scene ^js/Object group-1 ^js/Object group-2]
   (-> scene .-physics .-add (.overlap group-1 group-2)))
  (^js/Object [^js/Object scene ^js/Object group-1 ^js/Object group-2 callback-fn]
   (-> scene .-physics .-add (.overlap group-1 group-2 callback-fn))))

(defn world-enable! ^js/Object
  [^js/Object scene ^js/Object container]
  (-> scene .-physics .-world (.enable container)))
