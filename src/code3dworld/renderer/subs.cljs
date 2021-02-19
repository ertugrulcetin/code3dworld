(ns code3dworld.renderer.subs
  (:require
   [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::visibility
 (fn [db _]
   (:visibility db)))


(reg-sub
 ::console-visible?
 :<- [::visibility]
 (fn [visibility _]
   (:console? visibility)))


(reg-sub
 ::instruction-visible?
 :<- [::visibility]
 (fn [visibility _]
   (:instruction? visibility)))


(reg-sub
 ::instruction
 (fn [db _]
   (:instruction db)))
