(ns code3dworld.main.core
  (:require
   [goog.object :as ob]
   ["nrepl-client" :as nrepl]
   ["electron" :refer [app BrowserWindow crashReporter ipcMain]]))


(def main-window (atom nil))
(def backend-nrepl-port 3011)
(def client (delay ((ob/get nrepl "connect") #js {:port backend-nrepl-port})))


(defn init-browser []
  (reset! main-window (BrowserWindow.
                       (clj->js {:width 1200
                                 :height 800
                                 :webPreferences {:nodeIntegration true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL ^js/electron.BrowserWindow @main-window (str "file://" js/__dirname "/public/index.html"))
  (.on ^js/electron.BrowserWindow @main-window "closed" #(reset! main-window nil))
  (.on ipcMain "asynchronous-message" (fn [event code]
                                        (println "Code: " code)
                                        (.eval @client
                                               (str "(do\n"
                                                    '(in-ns 'backend-3d-scene.scene)
                                                    "(run " (pr-str code) ")"
                                                    "\n)")
                                               (fn [err result]
                                                 (println "Result" result " - Error: " err)
                                                 (.send (.-sender event) "asynchronous-reply"
                                                        (clj->js {:result result
                                                                  :error err}))))
                                        #_(.once @client "connect"
                                               (fn []
                                                 (println "Connected!")

                                                 ))))
  #_(.on js/process "uncaughtException" (fn [error]
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
