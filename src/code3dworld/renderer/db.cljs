(ns code3dworld.renderer.db
  (:require
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-cofx]]))


(def default-db
  {:name "code3dworld"
   :visibility {:console? true
                :instruction? true}
   :chapters {:intro {:title "Intro" :order 1}
              :chapter_1 {:title "Hello, World!" :order 2}
              :chapter_2 {:title "Math Time" :order 3}
              :chapter_3 {:title "3D Scene" :order 4}
              :chapter_4 {:title "Filtering Boxes" :order 5}}
   :active-chapter :intro})


(reg-cofx
 :settings
 (fn [cofx _]
   (assoc cofx :settings (util/settings))))
