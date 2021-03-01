(ns code3dworld.renderer.db
  (:require
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-cofx]]))


(def default-db
  {:name "code3dworld"
   :visibility {:console? true
                :instruction? true}
   :chapters {:intro {:title "Intro"}
              :chapter_1 {:title "Hello, World!"}
              :chapter_2 {:title "Math Time"}
              :chapter_3 {:title "3D Scene"}
              :chapter_4 {:title "Filtering Boxes"}}
   :active-chapter :intro
   :chapters-order [:intro :chapter_1 :chapter_2 :chapter_3 :chapter_4]})


(reg-cofx
 :settings
 (fn [cofx _]
   (assoc cofx :settings (util/settings))))
