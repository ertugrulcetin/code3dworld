(ns code3dworld.main.core
  (:require
   [goog.object :as ob]
   ["nrepl-client" :as nrepl]
   ["electron" :refer [app BrowserWindow crashReporter ipcMain]]))


(def main-window (atom nil))
(def backend-nrepl-port 3011)

;;TODO change this, if the new instance runs? will be broken pipe
(def client (delay ((ob/get nrepl "connect") #js {:port backend-nrepl-port})))


(defn- ^js/electron.BrowserWindow get-main-window []
  @main-window)


(defn init-browser []
  (reset! main-window (BrowserWindow.
                       (clj->js {:width 1200
                                 :height 800
                                 :webPreferences {:nodeIntegration true}})))
  ; Path is relative to the compiled js file (main.js in our case)
  (.loadURL (get-main-window) (str "file://" js/__dirname "/public/index.html"))
  (.on (get-main-window) "close" #(.send (.-webContents (get-main-window)) "app-close"))
  (.on ipcMain "closed" #(when-not (= js/process.platform "darwin")
                           (.quit app)))
  (.on ipcMain "eval" (fn [event code]
                        (.eval @client
                               (str "(do\n"
                                    '(in-ns 'backend-3d-scene.scene)
                                    "(run " (pr-str code) ")"
                                    "\n)")
                               (fn [err result]
                                 (.send (.-sender event) "eval-response"
                                        (clj->js {:result result
                                                  :error err}))))))
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
  (.on app "ready" init-browser))
