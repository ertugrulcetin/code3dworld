(ns code3dworld.renderer.navbar.views
  (:require [reagent.core :as r]))

(defn- body-view []
  [:div.c3-navbar.bg-gray-900.text-gray-50])

(defn navbar-views []
  (r/create-class
   {:reagent-render (fn [] [body-view])}))
