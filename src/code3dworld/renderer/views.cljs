(ns code3dworld.renderer.views
  (:require
   [reagent.core :as r]
   [goog.object :as ob]
   [goog.functions :as gf]
   [goog.dom :as dom]
   [code3dworld.renderer.util :as util]
   [code3dworld.renderer.subs :as subs]
   [code3dworld.renderer.events :as events]
   [re-frame.core :refer [dispatch dispatch-sync subscribe]]
   ["codemirror" :as cm]
   ["/vendor/split" :as split]
   ["codemirror/mode/clojure/clojure"]
   ["codemirror/addon/selection/active-line"]
   ["codemirror/addon/edit/matchbrackets"]
   ["codemirror/addon/edit/closebrackets"]))

(enable-console-print!)


(def from-textarea (ob/get cm "fromTextArea"))
#_(def ipc-renderer (.-ipcRenderer (js/require "electron")))
(def on-change-editor (gf/debounce (fn [key e] (dispatch-sync [key e])) 500))


(defonce vertical-split (r/atom nil))
(defonce horizontal-split (r/atom nil))
(defonce c3-editor (r/atom nil))


(defn tooltip [opt element]
  [:div.c3-tooltip-wrapper
   {:class (:class opt)}
   element
   [:div.tooltip (:text opt)]])


(defn- editor-body-view []
  [:div#editor.c3-editor
   [:textarea#c3-code-editor]])


(defn- boot-code-editor []
  (reset!
   c3-editor
   (from-textarea
    (dom/getElement "c3-code-editor")
    (clj->js {:lineNumbers true
              :matchBrackets true
              :autoCloseBrackets true
              :styleActiveLine true
              :styleActiveSelected true
              :mode "clojure"
              :theme "darcula c3-code-editor"})))
  (when-let [code (:code @(subscribe [::subs/chapter]))]
    (.setValue @c3-editor code))
  (dispatch [::events/update-editor-font-size])
  (.on @c3-editor "change" #(on-change-editor ::events/save-editor-content (.getValue @c3-editor))))


(defn- editor []
  (r/create-class
   {:component-did-mount boot-code-editor
    :reagent-render (fn [] [editor-body-view])}))


(defn- instruction-title []
  (let [chapter @(subscribe [::subs/chapter])]
    [:div.c3-instruction-title.p-10
     [:img.icon-2x.mr-5
      {:src "img/book.svg"}]
     [:span "Learn"]
     [:div.c3-chapter-status
      [:img.icon-2x
       {:src (util/format "img/%s.svg" (if (:done? chapter) "done" "check"))}]]]))


(defn- boot-instructions [chapter]
  (reset!
   horizontal-split
   (split #js ["#instructions" "#code"]
          (clj->js {:sizes [200 300]
                    :gutterSize 20
                    :dragInterval 0.5})))
  (when chapter
    (util/read-edn
     (str "resources/chapters/" (name chapter) ".edn")
     #(dispatch [::events/set-data [:instruction] %]))))


(defn- boot-instruction-body []
  (doseq [element (array-seq (dom/getElementsByClass "c3-code-preview"))]
    (from-textarea
     element
     (clj->js {:readOnly true
               :cursorBlinkRate -1
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
                      (let [body @(subscribe [::subs/instruction])]
                        [:div#instructions.c3-instructions
                         [instruction-title]
                         (when body
                           ^{:key body}
                           [instruction-body body])]))}))


(defn- editor-action-box []
  (let [visibility @(subscribe [::subs/visibility])
        console? (:console? visibility)
        instruction? (:instruction? visibility)]
    [:div.c3-editor-action
     [:div.c3-run-button
      "Run"]
     [tooltip
      {:text (if (and console? instruction?)
               "Enter Full Screen"
               "Exit Full Screen")
       :class "c3-full-screen"}
      [:div
       {:on-click #(do (dispatch [::events/update-element-visibility :instruction?])
                       (dispatch [::events/update-element-visibility :console?]))}
       [:img
        {:src (if (and console? instruction?)
                "img/full-screen.svg"
                "img/exit-full-screen.svg")}]]]
     [tooltip
      {:text (if console?
               "Hide Console"
               "Show Console")
       :class "c3-command-window"}
      [:div
       {:on-click #(dispatch [::events/update-element-visibility :console?])}
       [:img
        {:src "img/command-window.svg"}]]]
     [tooltip
      {:text "Decrease Font"
       :class "c3-decrease-font"}
      [:div
       {:on-click #(dispatch [::events/update-editor-font-size -])}
       [:img
        {:src "img/decrease-font-size.svg"}]]]
     [tooltip
      {:text "Increase Font"
       :class "c3-increase-font"}
      [:div
       {:on-click #(dispatch [::events/update-editor-font-size +])}
       [:img
        {:src "img/increase-font-size.svg"}]]]
     [tooltip
      {:text "Run 3D Scene"
       :class "c3-play-button"}
      [:div
       [:img
        {:src "img/play.svg"}]]]]))


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
  (let [chapter-order-info @(subscribe [::subs/chapter-order-info])]
    [:<>
     [:div.c3-back-button
      [:button.c3-button
       {:on-click #(dispatch [::events/change-chapter :back])}
       "Back"]]
     [:div chapter-order-info]
     [:div.c3-next-button
      [:button.c3-button.c3-next-button
       {:on-click #(dispatch [::events/change-chapter :next])}
       "Next"]]]))


(defn- bottom []
  (let [chapter @(subscribe [::subs/chapter])]
    [:div.c3-bottom
     [:div.c3-bottom-left
      [:span (:title chapter)]]
     [:div.c3-bottom-action
      [bottom-action-box]]
     [:div.c3-bottom-right
      [:span "Feedback"]]]))


(defn- main []
  (let [chapter @(subscribe [::subs/active-chapter])
        instruction? @(subscribe [::subs/instruction-visible?])]
    [:div.c3-main
     {:key chapter}
     (when (and instruction? chapter)
       [instructions chapter])
     [code]]))


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
