(ns code3dworld.renderer.views
  (:require
   [reagent.core :as r]
   [goog.object :as ob]
   [goog.dom :as dom]
   ["codemirror" :as cm]
   ["codemirror/mode/clojure/clojure"]
   ["codemirror/addon/edit/matchbrackets"]
   ["codemirror/addon/edit/closebrackets"]))


(def from-textarea (ob/get cm "fromTextArea"))


(defn- editor-body-view []
  [:div.c3-dashboard-editor
   [:textarea#c3-code-editor]])



(defn- boot-code-editor []
  (from-textarea
   (dom/getElement "c3-code-editor")
   (clj->js {:lineNumbers true
             :matchBrackets true
             :autoCloseBrackets true
             :mode "clojure"
             :theme "monokai"})))


(defn- editor []
  (r/create-class
   {:component-did-mount boot-code-editor
    :reagent-render (fn [] [editor-body-view])}))


(defn- instruction-title []
  [:p.c3-dashboard-instruction-title.p-10
   [:img.icon-2x.mr-5
    {:src "/img/book.svg"}]
   [:span "Learn"]])


(defn- instructions []
  [:div.c3-dashboard-instructions
   [instruction-title]])


(defn- console []
  [:div.c3-dashboard-console.p-5])


(defn- code []
  [:div.c3-dashboard-code
   [editor]
   [console]])


(defn- bottom []
  [:div.c3-dashboard-bottom])


(defn- main []
  [:div.c3-dashboard-main
   [instructions]
   [code]])


(defn- body-view []
  [:div.c3-dashboard-container
   [main]
   [bottom]])


(defn main-panel []
  (r/create-class
   {:reagent-render (fn [] [body-view])}))
