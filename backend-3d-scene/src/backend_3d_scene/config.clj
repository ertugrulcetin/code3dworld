(ns backend-3d-scene.config
  (:require
   [cprop.core :refer [load-config]]
   [cprop.source :as source]
   [mount.core :refer [args defstate]]))


(defstate config
  :start
  (->> [(args)
        (source/from-system-props)
        (source/from-env)]
       (load-config :merge)
       (into (sorted-map))))
