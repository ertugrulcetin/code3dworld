(ns code3dworld.renderer.navigation.subs
  (:require
   [re-frame.core :refer [reg-sub]]))

(reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))
