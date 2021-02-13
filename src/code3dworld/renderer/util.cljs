(ns code3dworld.renderer.util)

(defn current-settings []
  "This function gets current settings from local storage"
  (try
    (into (sorted-map)
          (as-> (.getItem js/localStorage "current_settings") data
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
