(ns code3dworld.renderer.views
  (:require
   ["/vendor/split" :as split]
   [reagent.core :as r]
   [goog.object :as ob]
   [goog.dom :as dom]
   ["codemirror" :as cm]
   ["codemirror/mode/clojure/clojure"]
   ["codemirror/addon/edit/matchbrackets"]
   ["codemirror/addon/edit/closebrackets"]))

(enable-console-print!)


(def from-textarea (ob/get cm "fromTextArea"))
(def ipc-renderer (.-ipcRenderer (js/require "electron")))


(defn- editor-body-view []
  [:div#editor.c3-editor
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
  [:p.c3-instruction-title.p-10
   [:img.icon-2x.mr-5
    {:src "img/book.svg"}]
   [:span "Learn"]])


(defn- instructions []
  [:div#instructions.c3-instructions
   [instruction-title]])


(defn- editor-action-box []
  [:div.c3-editor-action
   [:div.c3-run-button "Run"]])


(defn- console []
  [:div.c3-console.p-5])


(defn- code []
  [:div#code.c3-code
   [editor]
   [:div#console.c3-action-container
    [editor-action-box]
    [console]]])


(defn- bottom-action-box []
  [:<>
   [:div.c3-back-button
    [:button.c3-button
     "Back"]]
   [:div "2/7"]
   [:div.c3-next-button
    [:button.c3-button.c3-next-button
     "Next"]]])


(defn- bottom []
  [:div.c3-bottom
   [:div.c3-bottom-left
    [:span "Lesson name!"]]
   [:div.c3-bottom-action
    [bottom-action-box]]
   [:div.c3-bottom-right
    [:span "Feedback"]]])


(defn- main []
  [:div.c3-main
   [instructions]
   [code]])


(defn- body-view []
  [:div.c3-dashboard-container
   [main]
   [bottom]])


(defn- boot-main-panel []
  (.on ipc-renderer "asynchronous-reply" (fn [event arg]
                                           (println event)
                                           (println "Main message:" arg)))
  (.send ipc-renderer "asynchronous-message" "ping")
  (split #js ["#instructions" "#code"]
         (clj->js {:sizes [150 300]
                   :gutterSize 20
                   :dragInterval 0.5}))
  (split #js ["#editor" "#console"]
         (clj->js {:sizes [300 100]
                   :direction "vertical"
                   :gutterSize 20
                   :dragInterval 0.5})))


(defn main-panel []
  (r/create-class
   {:component-did-mount boot-main-panel
    :reagent-render (fn [] [body-view])}))
