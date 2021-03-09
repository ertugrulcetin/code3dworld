(ns code3dworld.main.core
  (:require
   [goog.object :as ob]
   [clojure.string :as str]
   ["nrepl-client" :as nrepl]
   ["electron" :refer [app BrowserWindow crashReporter ipcMain]]))


(def main-window (atom nil))
(def backend-nrepl-port 3011)

(def ls (js/require "electron-localstorage"))


(defn- get-client []
  ((ob/get nrepl "connect") #js {:port backend-nrepl-port}))


(defn- ^js/electron.BrowserWindow get-main-window []
  @main-window)


(defn init-browser []
  (let [index-html (str "file://" js/__dirname "/public/index.html")
        clear-interval-id (atom nil)]
    (reset! main-window (BrowserWindow.
                         (clj->js {:width (max (or (.getItem ls "width") 800) 800)
                                   :height (max (or (.getItem ls "height") 600) 600)
                                   :webPreferences {:nodeIntegration true}})))
    ; Path is relative to the compiled js file (main.js in our case)
    (.loadURL (get-main-window) index-html)
    (.on (get-main-window) "close" #(.send (.-webContents (get-main-window)) "app-close"))
    (.on (get-main-window) "resize" (fn []
                                      (let [[w h] (js->clj (.getSize (get-main-window)))]
                                        (.setItem ls "width" w)
                                        (.setItem ls "height" h))))
    (.on ipcMain "closed" #(when-not (= js/process.platform "darwin")
                             (.quit app)))
    (.on ipcMain "eval" (fn [event code]
                          (let [client (get-client)]
                            (.eval client
                                   (str "(do\n"
                                        '(in-ns 'backend-3d-scene.scene)
                                        "(run " (pr-str code) ")"
                                        "\n)")
                                   (fn [err result]
                                     (.send (.-sender event) "eval-response" (clj->js {:result result
                                                                                       :error err}))
                                     (.end client))))))
    (.on ipcMain "feedback-link-clicked" (fn [_]
                                           (when-not @clear-interval-id
                                             (reset! clear-interval-id
                                                     (js/setInterval
                                                      (fn []
                                                        (when-let [url (some-> (get-main-window) .-webContents .getURL)]
                                                          (when-not (str/starts-with? url "file://")
                                                            (.loadURL (get-main-window) index-html)
                                                            (js/clearInterval @clear-interval-id)
                                                            (reset! clear-interval-id nil))))
                                                      500)))))
    #_(.on js/process "uncaughtException" (fn [error]
                                            (println "Here is the ERROR:" error)))))


(defn main []
  ; CrashReporter can just be omitted
  (.start crashReporter
          (clj->js
           {:companyName "MyAwesomeCompany"
            :productName "MyAwesomeApp"
            :submitURL "https://example.com/submit-url"
            :autoSubmit false}))
  (.on app "ready" init-browser))
