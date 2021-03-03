(ns backend-3d-scene.core
  (:require
   [backend-3d-scene.scene :as scene]
   [backend-3d-scene.nrepl :as nrepl]
   [mount.core :as mount])
  (:gen-class))


(defn -main [& args]
  (mount/start #'nrepl/repl-server
               #'scene/app)
  ;(mount/stop)
  ;(println args)
  )
