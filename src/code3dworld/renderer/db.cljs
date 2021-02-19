(ns code3dworld.renderer.db
  (:require
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-cofx]]))


(def default-db
  {:name "code3dworld"
   :visibility {:console? true
                :instruction? true}})


(reg-cofx
 :current-settings
 (fn [cofx _]
   (assoc cofx :current-settings (util/current-settings))))
