(ns backend-3d-scene.core
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [clojure.tools.namespace.find :as ns-find]
   [mount.core :as mount])
  (:import (java.util TimeZone Locale))
  (:gen-class))


(defn- load-defstate-nses []
  (doseq [ns* (->> (io/file "src")
                   (#(ns-find/find-ns-decls-in-dir % ns-find/clj))
                   (filter (comp :defstate? meta second))
                   (map second))]
    (require ns*)))


(defn- start-defstates [args]
  (load-defstate-nses)
  (doseq [component (-> args
                        mount/start-with-args
                        :started)]
    (log/info component "started")))


(defn -main [& args]
  (TimeZone/setDefault (TimeZone/getTimeZone "UTC"))
  (Locale/setDefault (Locale. "en" "US"))
  (start-defstates args))


(comment
  (-main))
