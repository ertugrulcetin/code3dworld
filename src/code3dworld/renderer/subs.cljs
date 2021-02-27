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
 ::chapters
 (fn [db _]
   (:chapters db)))


(reg-sub
 ::chapters-order
 (fn [db _]
   (:chapters-order db)))


(reg-sub
 ::chapter
 :<- [::chapters]
 :<- [::active-chapter]
 (fn [[chapters active-chapter] _]
   (active-chapter chapters)))


(reg-sub
 ::chapter-order-info
 :<- [::chapters-order]
 :<- [::active-chapter]
 (fn [[chapters-order active-chapter] _]
   (str (->> active-chapter (.indexOf chapters-order) inc)
        "/"
        (count chapters-order))))
