(ns code3dworld.renderer.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [code3dworld.renderer.views :as views]
   [code3dworld.renderer.events :as events]
   [code3dworld.renderer.config :as config]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))


(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/main-panel] root-el)))


(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))


(comment
 (shadow/watch :renderer)
 (shadow/repl :renderer)
 )
