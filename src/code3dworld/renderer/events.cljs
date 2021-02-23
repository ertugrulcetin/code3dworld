(ns code3dworld.renderer.events
  (:require
   [code3dworld.renderer.util :as util]
   [code3dworld.renderer.effects :as effects]
   [code3dworld.renderer.db :as db]
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))


(reg-event-fx
 ::initialize-db
 [(inject-cofx :settings)]
 (fn [{:keys [_ settings]}]
   {:db (merge db/default-db (update settings :active-chapter #(keyword %)))
    :dispatch [::update-editor-font-size]}))


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
 ::save-editor-content
 (fn [{:keys [db]} [_ code]]
   {:db (assoc-in db [:chapters (:active-chapter db) :code] code)
    :dispatch [::save-settings-to-local]}))


(reg-event-fx
 ::update-element-visibility
 (fn [{:keys [db]} [_ element-key]]
   (let [element (-> db :visibility element-key)]
     {:db (assoc-in db [:visibility element-key] (not element))
      :dispatch [::save-settings-to-local]})))


(reg-event-fx
 ::change-chapter
 (fn [{:keys [db]} [_ operation]]
   (let [active-ch-key (:active-chapter db)
         active-ch-order (-> db :chapters active-ch-key :order)
         new-ch-order (if (= operation :next) (inc active-ch-order) (dec active-ch-order))
         new-ch-order (if (or (< new-ch-order 1) (> new-ch-order (-> db :chapters count)))
                        active-ch-order
                        new-ch-order)
         new-ch-key (some #(when (= new-ch-order (-> % val :order)) (key %)) (:chapters db))]
     {:db (assoc db :active-chapter new-ch-key)
      :dispatch [::save-settings-to-local]})))


(reg-event-fx
 ::save-settings-to-local
 (fn [{:keys [db]} _]
   {::effects/set-item-to-local! {:key "settings"
                                  :val (select-keys db [:visibility :editor :chapters :active-chapter])}}))
