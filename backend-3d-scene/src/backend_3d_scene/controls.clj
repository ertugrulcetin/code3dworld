(ns backend-3d-scene.controls
  (:require
   [jme-clj.core :refer :all])
  (:import
   (com.jme3.input KeyInput MouseInput)))


(defn- on-action-listener []
  (action-listener
   (fn [name* pressed? _]
     (let [{:keys [player focus]} (get-state)]
       (cond
         (= ::jump name*) (when pressed?
                            (call* player :jump))
         (= ::esc name*) (when pressed?
                           (set* (fly-cam) :enabled false)
                           (set* (input-manager) :cursor-visible true)
                           (set-state :focus false))
         (and (not focus)
              (= ::click name*)) (when pressed?
                                   (set* (fly-cam) :enabled true)
                                   (set* (input-manager) :cursor-visible false)
                                   (set-state :focus true))
         :else (when (call* (fly-cam) :is-enabled)
                 (set-state :control [::user-input (-> name* name keyword)] pressed?)))))))


(defn- set-up-keys []
  (apply-input-mapping
   {:triggers {::click (mouse-trigger MouseInput/BUTTON_LEFT)
               ::left (key-trigger KeyInput/KEY_A)
               ::right (key-trigger KeyInput/KEY_D)
               ::up (key-trigger KeyInput/KEY_W)
               ::down (key-trigger KeyInput/KEY_S)
               ::jump (key-trigger KeyInput/KEY_SPACE)
               ::esc (key-trigger KeyInput/KEY_ESCAPE)}
    :listeners {(on-action-listener) [::click ::left ::right ::up ::down ::jump ::esc]}}))


(defn- get-available-loc [player terrain]
  (let [loc (get* player :physics-location)
        ^Float size (- (get* terrain :terrain-size) 2)
        x (Math/max ^Float (- size) (Math/min ^Float (.-x loc) size))
        z (Math/max ^Float (- size) (Math/min ^Float (.-z loc) size))]
    (doto loc
      (.setX x)
      (.setZ z))))


(defn create-user-input [player terrain]
  (control ::user-input
           :init (fn []
                   (set-up-keys)
                   {:walk-direction (vec3)
                    :cam-dir (vec3)
                    :cam-left (vec3)
                    :up false
                    :down false
                    :left false
                    :right false})
           :update (fn [tpf]
                     (let [state (get-state :control ::user-input)
                           cam-dir (-> (:cam-dir state) (setv (get* (cam) :direction)) (mult-loc 0.6))
                           cam-left (-> (:cam-left state) (setv (get* (cam) :left)) (mult-loc 0.4))
                           walk-direction (setv (:walk-direction state) 0 0 0)
                           walk-direction (cond-> walk-direction
                                            (:left state) (add-loc cam-left)
                                            (:right state) (add-loc (negate cam-left))
                                            (:up state) (add-loc cam-dir)
                                            (:down state) (add-loc (negate cam-dir)))
                           loc (get-available-loc player terrain)]
                       (set* player :walk-direction walk-direction)
                       (set* player :physics-location loc)
                       (set* (cam) :location loc)))))
