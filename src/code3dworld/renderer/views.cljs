(ns code3dworld.renderer.views
  (:require
   [reagent.core :as r]
   [goog.object :as ob]
   [goog.functions :as gf]
   [goog.dom :as dom]
   [code3dworld.renderer.util :as util]
   [code3dworld.renderer.subs :as subs]
   [code3dworld.renderer.events :as events]
   [code3dworld.renderer.config :as config]
   [clojure.string :as str]
   [clojure.set :as set]
   [cljs.reader :as reader]
   [re-frame.core :refer [dispatch dispatch-sync subscribe]]
   ["codemirror" :as cm]
   ["/vendor/split" :as split]
   ["codemirror/mode/clojure/clojure"]
   ["codemirror/addon/selection/active-line"]
   ["codemirror/addon/edit/matchbrackets"]
   ["codemirror/addon/edit/closebrackets"]))

(enable-console-print!)


(def findp (js/require "find-process"))
(def ipc-renderer (.-ipcRenderer (js/require "electron")))
(def dir (str js/__dirname "/.."))
(def fpath (js/require "path"))


(def from-textarea (ob/get cm "fromTextArea"))
(def on-change-editor (gf/debounce (fn [key e] (dispatch-sync [key e])) 500))


(defonce vertical-split (r/atom nil))
(defonce horizontal-split (r/atom nil))
(defonce c3-editor (r/atom nil))


(defn tooltip [opt element]
  [:div.c3-tooltip-wrapper
   {:class (:class opt)
    :on-click (:on-click opt)}
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
  (some->> (:code @(subscribe [::subs/chapter])) (.setValue @c3-editor))
  (dispatch [::events/update-editor-font-size-in-local])
  (.on @c3-editor "change" #(on-change-editor ::events/save-editor-content (.getValue @c3-editor))))


(defn- editor []
  (r/create-class
   {:component-did-mount boot-code-editor
    :reagent-render (fn [] [editor-body-view])}))


(defn- instruction-title []
  (let [chapter @(subscribe [::subs/chapter])]
    [:div.c3-instruction-title
     [:img.icon-2x.mr-5
      {:src "img/book.svg"}]
     [:span "Learn"]
     (when (:required-fns chapter)
       [:div.c3-chapter-status
        [:img.icon-2x
         {:src (str "img/" (if (:done? chapter) "done" "check") ".svg")}]])]))


(defn- boot-instructions [chapter]
  (reset!
   horizontal-split
   (split #js ["#instructions" "#code"]
          (clj->js {:sizes [200 300]
                    :gutterSize 20
                    :dragInterval 0.5})))
  (when chapter
    (util/read-edn
     (.join fpath dir (str "/chapters/" (name chapter) ".edn"))
     #(dispatch [::events/set-data :instruction %]))))


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


(defn- run-button []
  [:button.c3-run-button
   {:disabled (and (not config/dev?)
                   (not @(subscribe [::subs/scene-3d-pid])))
    :on-click (fn [_]
                (let [code (.getValue @c3-editor)]
                  (when-not (str/blank? code)
                    (.send ipc-renderer "eval" code))))}
   "Run"])


(defn- reset-button []
  [tooltip
   {:text "Reset Exercise"
    :class "c3-reset"
    :on-click #(dispatch [::events/set-data [:visibility :reset-modal?] true])}
   [:img
    {:src "img/reset.svg"}]])


(defn- full-screen-button []
  (let [full-screen? (:full-screen? @(subscribe [::subs/visibility]))]
    [tooltip
     {:text (if full-screen?
              "Exit Full Screen"
              "Enter Full Screen")
      :class "c3-full-screen"
      :on-click #(if full-screen?
                   (do
                     (dispatch [::events/set-element-visibility :instruction? true])
                     (dispatch [::events/set-element-visibility :console? true])
                     (dispatch [::events/set-element-visibility :full-screen? false]))
                   (do
                     (dispatch [::events/set-element-visibility :instruction? false])
                     (dispatch [::events/set-element-visibility :console? false])
                     (dispatch [::events/set-element-visibility :full-screen? true])))}
     [:img
      {:src (if full-screen?
              "img/exit-full-screen.svg"
              "img/full-screen.svg")}]]))


(defn- console-buttons []
  [:<>
   [tooltip
    {:text (if (:console? @(subscribe [::subs/visibility]))
             "Hide Console"
             "Show Console")
     :class "c3-command-window"
     :on-click #(dispatch [::events/update-element-visibility :console?])}
    [:img
     {:src "img/command-window.svg"}]]
   [tooltip
    {:text "Clear Console"
     :class "c3-clear-console"
     :on-click #(dispatch [::events/reset :console])}
    [:img
     {:src "img/trash.svg"}]]])


(defn- inc-dec-font-buttons []
  [:<>
   [tooltip
    {:text "Decrease Font"
     :class "c3-decrease-font"
     :on-click #(dispatch [::events/update-editor-font-size -])}
    [:img
     {:src "img/decrease-font-size.svg"}]]
   [tooltip
    {:text "Increase Font"
     :class "c3-increase-font"
     :on-click #(dispatch [::events/update-editor-font-size +])}
    [:img
     {:src "img/increase-font-size.svg"}]]])


(defn- start-stop-scene-button []
  (if @(subscribe [::subs/scene-3d-pid])
    [:button.c3-stop-button
     {:on-click #(dispatch [::events/stop-3d-scene])}
     "Stop 3D Scene"]
    [:button.c3-play-button
     {:on-click #(dispatch [::events/start-3d-scene])}
     "Start 3D Scene"]))


(defn- editor-action-box []
  [:div.c3-editor-action
   [run-button]
   [reset-button]
   [full-screen-button]
   [console-buttons]
   [inc-dec-font-buttons]
   [start-stop-scene-button]])


(defn- get-out-style [type]
  (cond
    (= type :out-err) {:style {:color "#d03636"}}
    (= type :success) {:style {:color "#0eb525"}}))


(defn- console []
  (r/create-class
   {:component-did-mount #(do (dispatch [::events/console-scroll-to-bottom])
                              (reset!
                               vertical-split
                               (split #js ["#editor" "#console"]
                                      (clj->js {:sizes [300 100]
                                                :direction "vertical"
                                                :gutterSize 20
                                                :dragInterval 0.5}))))
    :component-will-unmount #(.destroy @vertical-split)
    :component-did-update #(dispatch [::events/console-scroll-to-bottom])
    :reagent-render (fn []
                      [:div#console-body.c3-console
                       (for [out @(subscribe [::subs/console])]
                         (map (fn [[i s]]
                                (if (str/blank? s)
                                  ^{:key i} [:br]
                                  ^{:key i} [:p (get-out-style (:type out))
                                             s]))
                              (map-indexed vector (str/split (:content out) #"\n"))))])}))


(defn- code []
  [:div#code.c3-code
   [editor]
   [:div#console.c3-action-container
    [editor-action-box]
    (when @(subscribe [::subs/console-visible?])
      [console])]])


(defn- bottom-action-box []
  (let [current-chapter-page-info @(subscribe [::subs/current-chapter-page-info])]
    [:<>
     [:div.c3-back-button
      [:button.c3-button
       {:on-click #(dispatch [::events/change-chapter :back])}
       "Back"]]
     [:div current-chapter-page-info]
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
      [:a#feedback-link.typeform-share.button
       {:href "https://form.typeform.com/to/x2kJ1U0x?typeform-medium=embed-snippet"
        :data-mode "popup"
        :data-size "70"
        :style {:visibility "hidden"}
        :target "_blank"}]
      [:a.feedback
       {:on-click (fn [e]
                    (.preventDefault e)
                    (if js/navigator.onLine
                      (do
                        (.send ipc-renderer "feedback-link-clicked")
                        (some-> (dom/getElement "feedback-link") .click))
                      (js/alert "Please check your internet connection.")))}
       "Share Feedback"]]]))


(defn- main []
  (let [chapter @(subscribe [::subs/active-chapter])
        instruction? @(subscribe [::subs/instruction-visible?])]
    [:div.c3-main
     {:key chapter}
     (when (and instruction? chapter)
       [instructions chapter])
     [code]]))


(defn- reset-exercise-modal []
  [:div.c3-modal
   {:on-click #(.stopPropagation %)}
   [:div.c3-container
    [:h2.c3-header "Reset Exercise"]
    [:p "Are you sure you want to restart? All of your this exercise code will be erased."]
    [:div.c3-action-box
     [:button.c3-reset-button
      {:on-click #(do (.setValue @c3-editor "")
                      (dispatch [::events/reset-exercise]))}
      "Reset"]
     [:button.c3-cancel-button
      {:on-click #(dispatch [::events/set-data [:visibility :reset-modal?] false])}
      "Cancel"]]]])


(defn- body-view []
  [:<>
   (when (true? @(subscribe [::subs/reset-modal-visible?]))
     [reset-exercise-modal])
   [:div.c3-dashboard-container
    [main]
    [bottom]]])


(defn- add-msg-to-console [content type]
  (when-not (str/blank? content)
    (dispatch-sync [::events/update-data :console (fnil conj [])
                    {:type type
                     :content content}])))


(defn- check-required-fns-used [used-fns]
  (let [chapter @(subscribe [::subs/chapter])
        required-fns (set (map symbol (:required-fns chapter)))
        missing-fns (seq (set/difference required-fns used-fns))
        done? (:done? chapter)]
    (when (and required-fns (not done?))
      (if missing-fns
        (add-msg-to-console (str "Required functions haven't been used: " (str/join ", " missing-fns)) :out-err)
        (do
          (add-msg-to-console "You've successfully completed the chapter!" :success)
          (dispatch [::events/mark-as-done]))))))


(defn- on-eval-response [_ response]
  (let [{:keys [result]} (js->clj response :keywordize-keys true)
        value (some->> result
                       (filter :value)
                       first
                       :value
                       reader/read-string)]
    (add-msg-to-console (:out value) :out)
    (add-msg-to-console (:out-err value) :out-err)
    (add-msg-to-console (:error-msg value) :out-err)
    (check-required-fns-used (:used-fns value))))


(defn- on-app-close []
  (dispatch-sync [::events/stop-3d-scene])
  (.send ipc-renderer "closed"))


(defn- check-scene-3d-pid-regularly []
  (js/setInterval (fn []
                    (when-let [pid @(subscribe [::subs/scene-3d-pid])]
                      (.then (findp "pid" pid)
                             (fn [list]
                               (when (empty? (js->clj list))
                                 (dispatch [::events/reset :scene-3d-pid])))
                             (fn [err]
                               (println "Err: " err)))))
                  500))


(defn- init []
  (dispatch [::events/stop-leftover-3d-scene])
  (.on ipc-renderer "eval-response" on-eval-response)
  (.on ipc-renderer "app-close" on-app-close)
  (check-scene-3d-pid-regularly))


(defn main-panel []
  (r/create-class
   {:component-did-mount #(init)
    :reagent-render (fn [] [body-view])}))
