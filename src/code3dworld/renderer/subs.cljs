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


(reg-sub
 ::active-chapter
 (fn [db _]
   (:active-chapter db)))


(reg-sub
 ::chapter
 (fn [db _]
   (let [active (:active-chapter db)]
     (-> db :chapters active))))


(reg-sub
 ::chapter-order-info
 (fn [db _]
   (let [active (:active-chapter db)]
     (str (-> db :chapters active :order)
          "/"
          (-> db :chapters count)))))
