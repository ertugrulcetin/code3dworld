(ns code3dworld.renderer.events
  (:require
   [code3dworld.renderer.util :as util]
   [code3dworld.renderer.effects :as effects]
   [code3dworld.renderer.db :as db]
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


(reg-event-fx
 ::set-editor-font-size
 (fn [{:keys [db]} [_ sym]]
   (let [font-size (or (-> db :editor :font-size) 18)
         font-size (sym font-size 1)]
     (when (and (>= font-size 14) (<= font-size 36))
       {:db (assoc-in db [:editor :font-size] font-size)
        :dispatch-n [[::update-editor-font-size]
                     [::save-settings-to-local]]}))))


(reg-event-fx
 ::update-editor-font-size
 (fn [{:keys [db]} _]
   (when-let [size (-> db :editor :font-size)]
     {::effects/set-editor-font-size! {:class-name "cm-s-c3-code-editor"
                                       :value (str size "px")}})))


(reg-event-fx
 ::update-element-visibility
 (fn [{:keys [db]} [_ element-key]]
   (let [element (-> db :visibility element-key)]
     {:db (assoc-in db [:visibility element-key] (not element))
      :dispatch [::save-settings-to-local]})))


(reg-event-fx
 ::save-settings-to-local
 (fn [{:keys [db]} _]
   {::effects/set-item-to-local! {:key "settings"
                                  :val (select-keys db [:visibility :editor])}}))
