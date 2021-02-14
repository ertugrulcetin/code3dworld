(ns code3dworld.renderer.events
  (:require
   [code3dworld.renderer.db :as db]
   [code3dworld.renderer.util :as util]
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))


(reg-event-fx
 ::initialize-db
 [(inject-cofx :current-settings)]
 (fn [{:keys [_ current-settings]}]
   {:db (assoc db/default-db :current-settings (util/current-settings))}))


(reg-event-db
 ::reset-in
 (fn [db [_ ks]]
   (util/dissoc-in db ks)))


(reg-event-db
 ::reset
 (fn [db [_ k]]
   (dissoc db k)))


(reg-event-db
 ::set-data
 (fn [db [_ path value]]
   (assoc-in db path value)))


(reg-event-db
 ::update-data
 (fn [db [_ key-seq f & args]]
   (let [key-seq (if (vector? key-seq) key-seq [key-seq])]
     (apply update-in (concat [db key-seq f] args)))))
