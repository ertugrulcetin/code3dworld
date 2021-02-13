(ns code3dworld.renderer.navigation.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [code3dworld.renderer.navigation.subs :as subs]
            [code3dworld.renderer.dashboard.views :refer [dashboard-views]]))

(def ^:private panels
  {:dashboard [dashboard-views]})

(defn- show-panel [active-panel]
  [panels active-panel])

(defn main-panel []
  (let [active-panel (subscribe [::subs/active-panel])]
    [show-panel @active-panel]))
