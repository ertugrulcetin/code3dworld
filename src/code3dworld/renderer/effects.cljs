(ns code3dworld.renderer.effects
  (:require [goog.dom :as dom]
            [re-frame.core :refer [reg-fx dispatch]]))


(def exec (.-exec (js/require "child_process")))


(reg-fx
 ::update-editor-font-size!
 (fn [{:keys [class-name value]}]
   (set!
    (.. (dom/getElementByClass class-name) -style -fontSize)
    value)))


(reg-fx
 ::set-item-to-local!
 (fn [{:keys [key val]}]
   (try
     (.setItem (.-localStorage js/window) key (.stringify js/JSON (clj->js val)))
     (catch js/Error e
       (println e)))))

(reg-fx
 ::remove-item-from-local!
 (fn [key]
   (try
     (.removeItem (.-localStorage js/window) key)
     (catch js/Error e
       (println e)))))


(reg-fx
 ::start-process
 (fn [path]
   (let [r (exec path)
         pid ^js/Number (.-pid r)]
     (dispatch [:code3dworld.renderer.events/set-data :scene-3d-pid pid]))))


(reg-fx
 ::kill-process
 (fn [pid]
   (.kill js/process pid)))
