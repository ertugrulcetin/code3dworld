(ns code3dworld.renderer.dashboard.editor-views
  (:require [reagent.core :as r]))


(defn- body-view []
  [:textarea#c3-code-editor])


(defn- boot-code-editor []
  (.fromTextArea js/CodeMirror
                 (.getElementById js/document "c3-code-editor")
                 (clj->js {:lineNumbers true
                           :mode        "clojure"
                           :theme       "monokai"})))


(defn- editor-views []
  (r/create-class
   {:component-did-mount boot-code-editor
    :reagent-render      (fn [] [body-view])}))

