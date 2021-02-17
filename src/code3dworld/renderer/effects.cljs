(ns code3dworld.renderer.effects
  (:require [goog.dom :as dom] [re-frame.core :refer [reg-fx]]))


(reg-fx
 :set-editor-font-size!
 (fn [{:keys [class-name value]}]
   (set!
    (.. (dom/getElementByClass class-name) -style -fontSize)
    value)))
