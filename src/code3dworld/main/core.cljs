(ns code3dworld.main.core
  (:require
   [goog.object :as ob]
   ["nrepl-client" :as nrepl]
   ["electron" :refer [app BrowserWindow crashReporter ipcMain]]))


(def main-window (atom nil))
(def client (delay ((ob/get nrepl "connect") #js {:port 7889})))

;; We're going to implement this, the structure is ready.
#_(.once @client "connect" (fn []
                             (println "Connected!")
                             (.eval @client
                                    "(+ 3 4)"
                                    (fn [err result]
                                      (println "Result" result " - Error: " err)
                                      (.end @client)))))


(defn init-browser []
  (reset! main-window (BrowserWindow.
                       (clj->js {:width 800
                                 :height 600
                                 :webPreferences {:nodeIntegration true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil))
  (.on ipcMain "asynchronous-message" (fn [event arg]
                                        (println "Renderer message:" arg)
                                        (.send (.-sender event) "asynchronous-reply" "pong")))
  (.on js/process "uncaughtException" (fn [error]
                                        (println "Here is the ERROR:" error))))


(defn main []
  ; CrashReporter can just be omitted
  (.start crashReporter
          (clj->js
           {:companyName "MyAwesomeCompany"
            :productName "MyAwesomeApp"
            :submitURL "https://example.com/submit-url"
            :autoSubmit false}))
  (.on app "window-all-closed" #(when-not (= js/process.platform "darwin")
                                  (.quit app)))
  (.on app "ready" init-browser))
