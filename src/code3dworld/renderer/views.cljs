(ns code3dworld.renderer.views
  (:require
   [code3dworld.renderer.subs :as subs]
   [re-frame.core :as re-frame]))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1 "Hello from " @name]]))
