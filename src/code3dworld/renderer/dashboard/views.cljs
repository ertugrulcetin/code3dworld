(ns code3dworld.renderer.dashboard.views
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [code3dworld.renderer.dashboard.subs :as subs]
            [code3dworld.renderer.dashboard.editor-views :refer [editor-views]]))

(defn- instruction-title-view [title]
  [:p.c3-dashboard-instruction-title.bg-gray-100.text-gray-900.p-5
   [:span.fa.fa-book.mr-3]
   [:span title]])

(defn- instructions-view []
  [:div.c3-dashboard-instructions
   [instruction-title-view "Learn"]])

(defn- editor-view []
  [:div.c3-dashboard-editor
   [editor-views]])

(defn- console-view []
  [:div.c3-dashboard-console.bg-gray-700.text-gray-300.p-5])

(defn- code-view []
  [:div.c3-dashboard-code
   [editor-view]
   [console-view]])

(defn- bottom-view []
  [:div.c3-dashboard-bottom.bg-gray-900.text-gray-50])

(defn- main-view []
  [:div.c3-dashboard-main
   [instructions-view]
   [code-view]])

(defn- body-view []
  [:div.c3-dashboard-container
   [main-view]
   [bottom-view]])

(defn dashboard-views []
  (r/create-class
   {:reagent-render (fn [] [body-view])}))
