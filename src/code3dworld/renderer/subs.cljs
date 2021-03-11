(ns code3dworld.renderer.subs
  (:require
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-sub]]))


(reg-sub
 ::console
 (fn [db _]
   (:console db)))


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
 ::reset-modal-visible?
 :<- [::visibility]
 (fn [visibility _]
   (:reset-modal? visibility)))


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
   (util/get-chapters-order (:chapters db))))


(reg-sub
 ::chapter
 :<- [::chapters]
 :<- [::active-chapter]
 (fn [[chapters active-chapter] _]
   (active-chapter chapters)))


(reg-sub
 ::current-chapter-page-info
 :<- [::chapters-order]
 :<- [::active-chapter]
 (fn [[chapters-order active-chapter] _]
   (str (->> active-chapter (.indexOf chapters-order) inc)
        "/"
        (count chapters-order))))


(reg-sub
 ::scene-3d-pid
 (fn [db]
   (:scene-3d-pid db)))


(reg-sub
 ::split-sizes
 (fn [db [_ k]]
   (-> db :editor k)))


(reg-sub
 ::last-chapter?
 :<- [::chapters-order]
 :<- [::active-chapter]
 (fn [[chapters-order active-chapter] _]
   (= (last chapters-order) active-chapter)))
