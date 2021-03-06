(ns code3dworld.renderer.util
  (:require [goog.string :as gstring]
            [cljs.reader :as reader]))


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


(defn dissoc-in
  ([m ks]
   (if-let [[k & ks] (seq ks)]
     (if (seq ks)
       (let [v (dissoc-in (get m k) ks)]
         (if (empty? v)
           (dissoc m k)
           (assoc m k v)))
       (dissoc m k))
     m))
  ([m ks & kss]
   (if-let [[ks' & kss] (seq kss)]
     (recur (dissoc-in m ks) ks' kss)
     (dissoc-in m ks))))


(defn read-edn [path f]
  (.readFile fs path "utf8"
             (fn [_ data]
               (f (reader/read-string data)))))


(defn format
  [& args]
  (apply gstring/format args))


(defn get-chapters-order [chapters]
  (->> chapters
       (reduce-kv (fn [m k v] (assoc m k (assoc v :id k))) {})
       vals
       (sort-by :order)
       (mapv :id)))


(defn get-pid []
  (get-item! "pid"))
