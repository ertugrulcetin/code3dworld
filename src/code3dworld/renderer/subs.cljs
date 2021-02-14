(ns code3dworld.renderer.subs
  (:require
   [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::dashboard
 (fn [db _]
   (:dashboard db)))


(reg-sub
 ::visibility
 :<- [::dashboard]
 :visibility)
