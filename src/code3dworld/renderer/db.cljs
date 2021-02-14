(ns code3dworld.renderer.db
  (:require
   [re-frame.core :refer [reg-cofx]]
   [code3dworld.renderer.util :as util]))


(def default-db
  {:name "code3dworld"})


(reg-cofx
 :current-settings
 (fn [cofx _]
   (assoc cofx :current-settings (util/current-settings))))
