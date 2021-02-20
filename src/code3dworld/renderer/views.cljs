(ns code3dworld.renderer.views
  (:require
   [reagent.core :as r]
   [goog.object :as ob]
   [goog.dom :as dom]
   [code3dworld.renderer.util :as util]
   [code3dworld.renderer.subs :as subs]
   [code3dworld.renderer.events :as events]
   [re-frame.core :refer [dispatch subscribe]]
   ["codemirror" :as cm]
   ["/vendor/split" :as split]
   ["codemirror/mode/clojure/clojure"]
   ["codemirror/addon/selection/active-line"]
   ["codemirror/addon/edit/matchbrackets"]
   ["codemirror/addon/edit/closebrackets"]))

(enable-console-print!)


(def from-textarea (ob/get cm "fromTextArea"))
#_(def ipc-renderer (.-ipcRenderer (js/require "electron")))


(defonce vertical-split (r/atom nil))
(defonce horizontal-split (r/atom nil))


(defn- editor-body-view []
  [:div#editor.c3-editor
   [:textarea#c3-code-editor]])


(defn- boot-code-editor []
  (from-textarea
   (dom/getElement "c3-code-editor")
   (clj->js {:lineNumbers true
             :matchBrackets true
             :autoCloseBrackets true
             :styleActiveLine true
             :styleActiveSelected true
             :mode "clojure"
             :theme "darcula"})))


(defn- editor []
  (r/create-class
   {:component-did-mount boot-code-editor
    :reagent-render (fn [] [editor-body-view])}))


(defn- instruction-title []
  [:p.c3-instruction-title.p-10
   [:img.icon-2x.mr-5
    {:src "img/book.svg"}]
   [:span "Learn"]])


(defn- boot-instructions [chapter]
  (reset!
   horizontal-split
   (split #js ["#instructions" "#code"]
          (clj->js {:sizes [150 300]
                    :gutterSize 20
                    :dragInterval 0.5})))
  (util/read-edn
   (str "resources/chapters/" chapter ".edn")
   #(dispatch [::events/set-data [:instruction] %])))


(defn- boot-instruction-body []
  (doseq [element (array-seq (dom/getElementsByClass "c3-code-preview"))]
    (from-textarea
     element
     (clj->js {:lineNumbers true
               :readOnly true
               :mode "clojure"
               :theme "darcula"}))))


(defn- instruction-body [body]
  (r/create-class
   {:component-did-mount boot-instruction-body
    :reagent-render (fn [] body)}))


(defn- instructions [chapter]
  (r/create-class
   {:component-did-mount #(boot-instructions chapter)
    :component-will-unmount #(.destroy @horizontal-split)
    :reagent-render (fn []
                      [:div#instructions.c3-instructions
                       [instruction-title]
                       (when-let [body @(subscribe [::subs/instruction])]
                         [instruction-body body])])}))


(defn- editor-action-box []
  [:div.c3-editor-action
   [:div.c3-run-button
    "Run"]
   [:div.c3-full-screen
    {:on-click #(do (dispatch [::events/update-element-visibility :instruction?])
                    (dispatch [::events/update-element-visibility :console?]))}
    [:img
     {:src "img/full-screen.svg"}]]
   [:div.c3-command-window
    {:on-click #(dispatch [::events/update-element-visibility :console?])}
    [:img
     {:src "img/command-window.svg"}]]
   [:div.c3-decrease-font
    {:on-click #(dispatch [::events/set-editor-font-size -])}
    [:img
     {:src "img/decrease-font-size.svg"}]]
   [:div.c3-increase-font
    {:on-click #(dispatch [::events/set-editor-font-size +])}
    [:img
     {:src "img/increase-font-size.svg"}]]])


(defn- console []
  (r/create-class
   {:component-did-mount #(reset!
                           vertical-split
                           (split #js ["#editor" "#console"]
                                  (clj->js {:sizes [300 100]
                                            :direction "vertical"
                                            :gutterSize 20
                                            :dragInterval 0.5})))
    :component-will-unmount #(.destroy @vertical-split)
    :reagent-render (fn []
                      [:div.c3-console
                       [:p "~ cd Projects/electron/code3dworld/"]
                       [:p "~ lein watch"]
                       [:p "~ electron ."]])}))


(defn- code []
  [:div#code.c3-code
   [editor]
   [:div#console.c3-action-container
    [editor-action-box]
    (when @(subscribe [::subs/console-visible?])
      [console])]])


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
   (when @(subscribe [::subs/instruction-visible?])
     [instructions "intro"])
   [code]])


(defn- body-view []
  [:div.c3-dashboard-container
   [main]
   [bottom]])


(defn- init-ipc []
  #_(.on ipc-renderer "asynchronous-reply" (fn [event arg]
                                             (println event)
                                             (println "Main message:" arg)))
  #_(.send ipc-renderer "asynchronous-message" "ping"))


(defn main-panel []
  (r/create-class
   {:component-did-mount #(init-ipc)
    :reagent-render (fn [] [body-view])}))
