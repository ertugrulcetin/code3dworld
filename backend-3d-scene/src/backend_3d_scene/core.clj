(ns backend-3d-scene.core
  (:require
   [backend-3d-scene.config :as config]
   [backend-3d-scene.scene :as scene]
   [backend-3d-scene.nrepl :as nrepl]
   [mount.core :as mount])
  (:import (java.util TimeZone Locale))
  (:gen-class))


(defn -main [& args]
  (TimeZone/setDefault (TimeZone/getTimeZone "UTC"))
  (Locale/setDefault (Locale. "en" "US"))
  (mount/start #'config/config #'nrepl/repl-server #'scene/app))


(comment
  (-main)
  (mount/stop))
