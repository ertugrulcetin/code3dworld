(ns code3dworld.renderer.db
  (:require
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-cofx]]))


(def default-db
  {:name "Code 3D World"
   :visibility {:console? true
                :instruction? true}
   :chapters {:intro {:title "Intro" :order 1}
              :running-3d-scene {:title "3D Scene" :order 2}
              :hello-world {:title "Hello, World"
                            :order 3
                            :required-fns '#{println}
                            :code "(println \"Hello, World!\")"}
              :math {:title "Math Time"
                     :order 4
                     :required-fns '#{+ - * /}}
              :objects {:title "3D Objects"
                        :order 5
                        :required-fns '#{throw-ball comment create-box}
                        :code "\n\n\n(comment\n (println \"I won't see this.\")\n (println \"Also this one too.\"))"}
              :filter {:title "The filter function"
                       :order 6
                       :required-fns '#{> < >= <= println}}
              :filter-boxes {:title "Filtering boxes"
                             :order 7
                             :required-fns '#{create-box println}}
              :map {:title "The map function"
                    :order 8
                    :required-fns '#{- * / println}}
              :map-boxes {:title "Mapping boxes"
                          :order 9
                          :required-fns '#{create-box
                                           apply-blue
                                           apply-green
                                           apply-red
                                           apply-original}}}
   :active-chapter :intro})


(reg-cofx
 :settings
 (fn [cofx _]
   (assoc cofx :settings (util/settings))))


(reg-cofx
 :pid
 (fn [cofx _]
   (assoc cofx :pid (util/get-pid))))
