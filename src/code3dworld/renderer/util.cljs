(ns code3dworld.renderer.util
  (:require [goog.string :as gstring] [cljs.reader :as reader]))

(def fs "Provides access to Node/Electron [fs library](https://nodejs.org/api/fs.html)." (js/require "fs"))


(defn settings
  "This function gets current settings from local storage"
  []
  (try
    (into (sorted-map)
          (as-> (.getItem js/localStorage "settings") data
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
