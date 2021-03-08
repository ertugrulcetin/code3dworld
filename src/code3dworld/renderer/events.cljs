(ns code3dworld.renderer.events
  (:require
   [code3dworld.renderer.util :as util]
   [code3dworld.renderer.effects :as effects]
   [code3dworld.renderer.db :as db]
   [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx]]))


(defn- need-init-db? [db]
  (and
   (:active-chapter db)
   ((set (util/get-chapters-order (:chapters db))) (:active-chapter db))))


(reg-event-fx
 ::initialize-db
 [(inject-cofx :settings) (inject-cofx :pid)]
 (fn [{:keys [_ settings pid]}]
   (let [settings (when-not (-> settings :active-chapter empty?)
                    (update settings :active-chapter keyword))
         db (merge db/default-db settings)
         db (if pid (assoc db :pid pid) db)]
     (if (or (nil? settings) (need-init-db? db))
       {:db db
        :dispatch [::update-editor-font-size-in-local]}
       {:db db/default-db
        ::effects/remove-item-from-local! "settings"}))))


(reg-event-fx
 ::console-scroll-to-bottom
 (fn [_ _]
   {::effects/scroll! {:source-id "console-body"}}))


(reg-event-db
 ::reset
 (fn [db [_ k]]
   (dissoc db k)))


(reg-event-db
 ::set-data
 (fn [db [_ path value]]
   (let [path (if (vector? path) path [path])]
     (assoc-in db path value))))


(reg-event-db
 ::update-data
 (fn [db [_ key-seq f & args]]
   (let [key-seq (if (vector? key-seq) key-seq [key-seq])]
     (apply update-in (concat [db key-seq f] args)))))


(reg-event-fx
 ::reset-exercise
 (fn [{:keys [db]}]
   {:db (-> db
            (update-in [:chapters (:active-chapter db)] dissoc :done? :code)
            (assoc-in [:visibility :reset-modal?] false))
    :dispatch [::save-settings-to-local]}))


(reg-event-fx
 ::update-editor-font-size
 (fn [{:keys [db]} [_ sym]]
   (let [font-size (or (-> db :editor :font-size) 18)
         font-size (sym font-size 1)]
     (when (and (>= font-size 14) (<= font-size 36))
       {:db (assoc-in db [:editor :font-size] font-size)
        :dispatch-n [[::update-editor-font-size-in-local]
                     [::save-settings-to-local]]}))))


(reg-event-fx
 ::update-editor-font-size-in-local
 (fn [{:keys [db]} _]
   (when-let [size (-> db :editor :font-size)]
     {::effects/update-editor-font-size! {:class-name "cm-s-c3-code-editor"
                                          :value (str size "px")}})))


(reg-event-fx
 ::save-editor-content
 (fn [{:keys [db]} [_ code]]
   {:db (assoc-in db [:chapters (:active-chapter db) :code] code)
    :dispatch [::save-settings-to-local]}))


(reg-event-fx
 ::update-element-visibility
 (fn [{:keys [db]} [_ element-key]]
   {:db (update-in db [:visibility element-key] not)
    :dispatch [::save-settings-to-local]}))


(reg-event-fx
 ::set-element-visibility
 (fn [{:keys [db]} [_ element-key v]]
   {:db (assoc-in db [:visibility element-key] v)
    :dispatch [::save-settings-to-local]}))


(reg-event-fx
 ::change-chapter
 (fn [{:keys [db]} [_ operation]]
   (let [current-chapter (:active-chapter db)
         [prev [_ next-chapter]] (split-with #(not= % current-chapter) (util/get-chapters-order (:chapters db)))
         new-chapter (if (= operation :next) next-chapter (last prev))]
     {:db (assoc db :active-chapter (or new-chapter current-chapter))
      :dispatch [::save-settings-to-local]})))


(reg-event-fx
 ::save-settings-to-local
 (fn [{:keys [db]} _]
   {::effects/set-item-to-local! {:key "settings"
                                  :val (-> db
                                           (select-keys [:visibility :editor :chapters :active-chapter])
                                           (update :visibility select-keys [:console? :instruction?]))}}))


(reg-event-fx
 ::start-3d-scene
 (fn [_ _]
   {::effects/start-process "/scene.app/Contents/MacOS/scene"}))


(reg-event-fx
 ::stop-3d-scene
 (fn [{:keys [db]} _]
   {:db (dissoc db :scene-3d-pid)
    ::effects/kill-process (:scene-3d-pid db)}))


(reg-event-fx
 ::stop-leftover-3d-scene
 (fn [{:keys [db]} _]
   {:db (dissoc db :pid)
    ::effects/kill-process (:pid db)}))


(reg-event-fx
 ::mark-as-done
 (fn [{:keys [db]} _]
   (let [db (assoc-in db [:chapters (:active-chapter db) :done?] true)]
     {:db db
      :dispatch [::save-settings-to-local]})))
