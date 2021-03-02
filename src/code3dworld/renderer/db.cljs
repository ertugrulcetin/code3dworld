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
              :chapter_4 {:title "The filter function" :order 5}
              :chapter_5 {:title "Filtering boxes" :order 6}
              :chapter_6 {:title "The map function" :order 7}
              :chapter_7 {:title "Mapping boxes" :order 8}}
   :active-chapter :intro})


(reg-cofx
 :settings
 (fn [cofx _]
   (assoc cofx :settings (util/settings))))
