(ns code3dworld.renderer.util
  (:require [cljs.reader :as reader]))


(def fs (js/require "fs"))


(defn set-item! [key val]
  (try
    (.setItem (.-localStorage js/window) key (.stringify js/JSON (clj->js val)))
    (catch js/Error e
      (println e))))


(defn remove-item! [key]
  (try
    (.removeItem (.-localStorage js/window) key)
    (catch js/Error e
      (println e))))


(defn get-item! [key]
  (.getItem js/localStorage key))


(defn settings
  "This function gets current settings from local storage"
  []
  (try
    (into (sorted-map)
          (as-> (get-item! "settings") data
            (.parse js/JSON data)
            (js->clj data :keywordize-keys true)))
    (catch js/Error _
      {})))


(defn read-edn [path f]
  (.readFile fs path "utf8"
             (fn [_ data]
               (f (reader/read-string data)))))


(defn get-chapters-order [chapters]
  (->> chapters
       (reduce-kv (fn [m k v] (assoc m k (assoc v :id k))) {})
       vals
       (sort-by :order)
       (mapv :id)))


(defn get-pid []
  (get-item! "pid"))
