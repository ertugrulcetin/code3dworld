(ns backend-3d-scene.core
  (:require
   [backend-3d-scene.api :as api]
   [backend-3d-scene.nrepl :as nrepl]
   [mount.core :as mount])
  (:gen-class))


(defn -main [& args]
  (mount/start #'nrepl/repl-server
               #'api/app)
  (mount/stop)
  (println args))
