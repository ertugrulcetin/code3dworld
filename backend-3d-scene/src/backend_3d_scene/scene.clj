(ns backend-3d-scene.scene
  (:require
   [backend-3d-scene.controls :as co]
   [jme-clj.core :refer :all]
   [mount.core :refer [defstate]]
   [com.rpl.specter :as s])
  (:import
   (com.jme3.math ColorRGBA Vector3f)
   (com.jme3.terrain.heightmap HillHeightMap)
   (com.jme3.texture Texture$WrapMode)
   (com.jme3.bullet.collision.shapes BoxCollisionShape)))


(defn- raise
  ([msg]
   (raise msg {}))
  ([msg map]
   (raise msg map nil))
  ([msg map cause]
   (throw (ex-info msg map cause))))


(defn- create-player []
  (let [player (character-control (capsule-collision-shape 3 7 1) 0.05)]
    (setc player
          :jump-speed 20
          :fall-speed 30
          :gravity 30
          :physics-location (vec3 0 0 0))))


(defn- create-material []
  (let [grass (set* (load-texture "Textures/Terrain/splat/grass.jpg") :wrap Texture$WrapMode/Repeat)
        dirt (set* (load-texture "Textures/Terrain/splat/dirt.jpg") :wrap Texture$WrapMode/Repeat)
        rock (set* (load-texture "Textures/Terrain/splat/road.jpg") :wrap Texture$WrapMode/Repeat)]
    (-> (material "Common/MatDefs/Terrain/Terrain.j3md")
        (set* :texture "Alpha" (load-texture "Textures/Terrain/splat/alphamap.png"))
        (set* :texture "Tex1" grass)
        (set* :float "Tex1Scale" (float 64))
        (set* :texture "Tex2" dirt)
        (set* :float "Tex2Scale" (float 32))
        (set* :texture "Tex3" rock)
        (set* :float "Tex3Scale" (float 128)))))


(defn- create-terrain [mat]
  (let [_ (set! (HillHeightMap/NORMALIZE_RANGE) 100)
        height-map (hill-height-map 513 100 50 100 (byte 3))
        _ (call* height-map :load)
        patch-size 65
        terrain (terrain-quad "my terrain" patch-size 513 (get-height-map height-map))]
    (-> terrain
        (setc :material mat
              :local-translation [0 -100 0]
              :local-scale [2 1 2])
        (add-control (terrain-lod-control terrain (cam))))))


(defn- add-lights []
  (let [sun (-> (light :directional)
                (setc :direction (vec3 -0.5 -0.5 -0.5)
                      :color ColorRGBA/White))
        sun-b (-> (light :directional)
                  (setc :direction (vec3 0.5 0.5 0.5)
                        :color ColorRGBA/White))
        ambient (-> (light :ambient)
                    (set* :color ColorRGBA/White))]
    (add-light-to-root sun)
    (add-light-to-root sun)
    (add-light-to-root sun-b)
    (add-light-to-root sun-b)
    (add-light-to-root ambient)))


(defn init []
  (setc (fly-cam)
        :move-speed 100
        :zoom-speed 0)
  (let [bas (attach (bullet-app-state))
        mat (create-material)
        terrain (create-terrain mat)
        terrain-shape (create-mesh-shape terrain)
        landscape (rigid-body-control terrain-shape 0)
        player (create-player)
        spatial (node "player node")]
    (add-lights)
    (add-to-root (create-sky "Textures/Sky/Bright/BrightSky.dds" :cube))
    (-> spatial
        (add-control player)
        (add-control (co/create-user-input player terrain))
        (add-to-root))
    (-> terrain
        (add-control landscape)
        (add-to-root))
    (-> bas
        (get* :physics-space)
        (call* :add landscape))
    (-> bas
        (get* :physics-space)
        (call* :add player))
    {:bullet-app-state bas
     :player player
     :terrain terrain}))


(defn get-all-boxes []
  (get-state :app :boxes))


(defn get-boxes []
  (vec (select-keys (get-all-boxes) [:name :size])))


(defn- print-err [msg]
  (.println (System/err) (str "Warning: " msg)))


(defn create-box [{:keys [name size random-location?] :or {size 5} :as opts}]
  (if ((set (map :name (get-all-boxes))) name)
    (print-err (format "There is a box with name `%s` already. You need to create a box with a different name." name))
    (let [texture (load-texture "Textures/2D/box.jpg")
          mat (set* (unshaded-mat) :texture "ColorMap" texture)
          r (ray (.getLocation (cam)) (.getDirection (cam)))
          dir (.getDirection r)
          origin (.getOrigin r)
          box* (setc (geo name (box size size size))
                     :local-translation (add origin (if random-location?
                                                      (add (mult dir 40)
                                                           (rand 15)
                                                           (rand 15)
                                                           (rand -15))
                                                      (mult dir (* 4 size))))
                     :material mat)
          box-cs (BoxCollisionShape. ^Vector3f (vec3 size size size))
          box-control (rigid-body-control box-cs 0)
          box* (-> box*
                   (add-control box-control)
                   (add-to-root))
          {bas :bullet-app-state} (get-state)]
      (-> bas
          (get* :physics-space)
          (call* :add box*))
      (update-state :app :boxes (fnil conj []) {:name name
                                                :size size
                                                :control box-control
                                                :box box*
                                                :color :original}))))


(defn remove-box [name]
  (if-let [{:keys [box control]} (some #(when (= name (:name %)) %) (get-all-boxes))]
    (let [{bas :bullet-app-state} (get-state)]
      (-> bas
          (get* :physics-space)
          (call* :remove-all box))
      (remove-from-root box)
      (call* box :remove-control control)
      (update-state :app
                    :boxes
                    #(vec (remove (fn [b] (= (:name b) name)) %))))
    (print-err (format "There is no box with `%s` name." name))))


(defn remove-all-boxes []
  (doseq [b (get-all-boxes)]
    (remove-box (:name b))))


(defn get-box [name]
  (if-let [{:keys [box]} (some #(when (= name (:name %)) %) (get-all-boxes))]
    box
    (print-err (format "There is no box with `%s` name." name))))


(defn- apply-color [color-key box-name]
  (when-let [box (get-box box-name)]
    (let [texture (load-texture (case color-key
                                  :red "Textures/2D/rbox.jpg"
                                  :blue "Textures/2D/bbox.jpg"
                                  :green "Textures/2D/gbox.jpg"
                                  :original "Textures/2D/box.jpg"))
          mat (set* (unshaded-mat) :texture "ColorMap" texture)]
      (set* box :material mat)
      (s/transform [s/ATOM :jme-clj.core/app :boxes s/ALL #(= box-name (:name %))]
                   #(assoc % :color color-key)
                   states))))


(defn apply-red [box-name]
  (apply-color :red box-name))


(defn apply-green [box-name]
  (apply-color :green box-name))


(defn apply-blue [box-name]
  (apply-color :blue box-name))


(defn apply-original [box-name]
  (apply-color :original box-name))


(defstate ^{:on-reload :noop}
          app
          :start (do
                   (defsimpleapp app*
                                 :opts {:show-settings? false
                                        :pause-on-lost-focus? false
                                        :display-stat-view? false
                                        :display-fps? false
                                        :settings {:title "3D Scene"
                                                   :load-defaults? true
                                                   :frame-rate 60
                                                   :width 800
                                                   :height 600
                                                   :resizable? true}}
                                 :init init)
                   (start app*))
          :stop (unbind-app #'app*))

(comment
 (run app
      (apply-blue "ertu")
      (println (get-all-boxes))
      ;(remove-box "ertus")
      ;(re-init init)
      )
 (run app
      (remove-all-boxes)
      (println (get-all-boxes))
      (do
        (create-box {:name "box 1"
                     :size 8
                     :random-location? true})
        (create-box {:name "box 2"
                     :size 9
                     :random-location? true})
        (create-box {:name "box 3"
                     :size 10
                     :random-location? true})

        (apply-blue "box 1")
        (apply-red "box 2")
        (apply-green "box 3"))
      #_(create-box {:name (str (rand))
                     :size 5
                     :random-location? true})))
