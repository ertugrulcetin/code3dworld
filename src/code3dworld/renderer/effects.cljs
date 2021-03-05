(ns code3dworld.renderer.effects
  (:require [goog.dom :as dom]
            [code3dworld.renderer.util :as util]
            [re-frame.core :refer [reg-fx dispatch]]))


(def fpath (js/require "path"))
(def dir (str js/__dirname "/.."))
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
   (util/set-item! key val)))


(reg-fx
 ::remove-item-from-local!
 (fn [key]
   (util/remove-item! key)))


(reg-fx
 ::start-process
 (fn [path]
   (let [r (exec (.join fpath dir path))
         pid ^js/Number (.-pid r)]
     (util/set-item! "pid" pid)
     (dispatch [:code3dworld.renderer.events/set-data :scene-3d-pid pid]))))


(reg-fx
 ::kill-process
 (fn [pid]
   (util/remove-item! "pid")
   (.kill js/process pid)))
