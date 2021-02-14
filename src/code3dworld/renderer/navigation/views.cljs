(ns code3dworld.renderer.navigation.views
  (:require [code3dworld.renderer.dashboard.views :refer [dashboard-views]]
            [code3dworld.renderer.navbar.views :refer [navbar-views]]))


(defn main-panel []
  [:<>
   [navbar-views]
   [dashboard-views]])
