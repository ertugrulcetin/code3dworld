(ns code3dworld.renderer.db
  (:require
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-cofx]]))


(def default-db
  {:name "code3dworld"
   :visibility {:console? true
                :instruction? true}
   :chapters {:intro {:title "Intro"
                      :order 1}
              :chapter_1 {:title "Reducers"
                          :order 2}}
   :active-chapter :intro})


(reg-cofx
 :settings
 (fn [cofx _]
   (assoc cofx :settings (util/settings))))
