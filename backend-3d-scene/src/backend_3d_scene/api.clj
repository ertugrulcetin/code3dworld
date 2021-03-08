(ns backend-3d-scene.api
  (:require
   [backend-3d-scene.controls :as co]
   [com.rpl.specter :as s]
   [jme-clj.core :refer :all]
   [mount.core :refer [defstate]])
  (:import
   (com.jme3.app SimpleApplication)
   (com.jme3.bullet.collision.shapes BoxCollisionShape)
   (com.jme3.math ColorRGBA Vector3f FastMath)
   (com.jme3.scene.shape Sphere$TextureMode)
   (com.jme3.terrain.heightmap HillHeightMap)
   (com.jme3.texture Texture$WrapMode)))


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
                     :local-translation (if (:local-translation opts)
                                          (:local-translation opts)
                                          (add origin (if random-location?
                                                        (add (mult dir 40)
                                                             (rand 15)
                                                             (rand 15)
                                                             (rand -15))
                                                        (mult dir (* 4 size)))))
                     :material mat)
          box-cs (BoxCollisionShape. ^Vector3f (vec3 size size size))
          box-control (rigid-body-control box-cs 0)
          box* (-> box*
                   (add-control box-control)
                   (add-to-root))
          {bas :bullet-app-state} (get-state)
          box-m {:name name
                 :size size
                 :control box-control
                 :box box*
                 :color :original}]
      (-> bas
          (get* :physics-space)
          (call* :add box*))
      (update-state :app :boxes (fnil conj []) box-m)
      box-m)))


(defn init []
  (setc (fly-cam)
        :move-speed 100
        :zoom-speed 0)
  (.deleteMapping (input-manager) SimpleApplication/INPUT_MAPPING_EXIT)
  (let [bas (attach (bullet-app-state))
        mat (create-material)
        terrain (create-terrain mat)
        terrain-shape (create-mesh-shape terrain)
        landscape (rigid-body-control terrain-shape 0)
        player (create-player)
        spatial (node "player node")
        sphere* (set* (sphere 32 32 0.4 true false)
                      :texture-mode Sphere$TextureMode/Projected)
        stone-mat (set* (unshaded-mat)
                        :texture "ColorMap" (load-texture "Textures/Terrain/Rock/Rock.PNG"))]
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
     :terrain terrain
     :sphere sphere*
     :stone-mat stone-mat
     :focus true
     :total 0}))


(defn simple-update [tpf]
  (let [{:keys [total]} (get-state)
        total (+ total tpf)
        ;;overflow
        total (if (< total 0) 0 total)
        q (quat)
        _ (call* q :from-angle-axis total Vector3f/UNIT_Y)
        {:keys [box control]} (or (some #(when (= "Center Box" (:name %)) %) (get-all-boxes))
                                  (create-box {:name "Center Box"
                                               :local-translation (vec3 0 -50 -256)}))
        world-trans (get* box :world-translation)]
    (setc control
          :physics-rotation q
          :physics-location (vec3 (.-x world-trans)
                                  (+ (.-y world-trans) (/ (FastMath/sin total) 2))
                                  (.-z world-trans)))
    {:total total}))


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


(defn throw-ball
  ([]
   (throw-ball {}))
  ([{:keys [speed] :or {speed 50}}]
   (let [{:keys [sphere stone-mat bullet-app-state]} (get-state)
         r (ray (.getLocation (cam)) (.getDirection (cam)))
         ball-geo (-> (geo "cannon ball" sphere)
                      (setc :material stone-mat
                            :local-translation (add (get* (cam) :location)
                                                    (mult (.getDirection r) 10)))
                      (add-to-root))
         ball-phy (rigid-body-control 1.0)]
     (add-control ball-geo ball-phy)
     (-> bullet-app-state
         (get* :physics-space)
         (call* :add ball-phy))
     (set* ball-phy :linear-velocity (-> (cam)
                                         (get* :direction)
                                         (mult speed))))))
