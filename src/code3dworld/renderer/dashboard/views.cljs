(ns code3dworld.renderer.dashboard.views
  (:require
   [code3dworld.renderer.dashboard.editor-views :refer [editor-views]]
   [code3dworld.renderer.dashboard.subs :as subs]
   [re-frame.core :refer [dispatch subscribe]]
   [reagent.core :as r]))


(defn- instruction-title [title]
  [:p.c3-dashboard-instruction-title.bg-gray-100.text-gray-900.p-5
   [:span.fa.fa-book.mr-3]
   [:span title]])


(defn- instructions []
  [:div.c3-dashboard-instructions
   [instruction-title "Learn"]])


(defn- editor []
  [:div.c3-dashboard-editor
   [editor-views]])


(defn- console []
  [:div.c3-dashboard-console.bg-gray-700.text-gray-300.p-5])


(defn- code []
  [:div.c3-dashboard-code
   [editor]
   [console]])


(defn- bottom []
  [:div.c3-dashboard-bottom.bg-gray-900.text-gray-50])


(defn- main []
  [:div.c3-dashboard-main
   [instructions]
   [code]])


(defn- body-view []
  [:div.c3-dashboard-container
   [main]
   [bottom]])


(defn dashboard-views []
  (r/create-class
   {:reagent-render (fn [] [body-view])}))
