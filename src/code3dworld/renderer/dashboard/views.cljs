(ns code3dworld.renderer.dashboard.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [code3dworld.renderer.dashboard.subs :as subs]))

(defn dashboard-views []
  (r/create-class
   {:reagent-render (fn []
                      [:div "Dashboard"])}))
