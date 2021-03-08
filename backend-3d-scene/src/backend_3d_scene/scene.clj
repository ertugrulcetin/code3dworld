(ns backend-3d-scene.scene
  (:require
   [backend-3d-scene.api :refer :all]
   [backend-3d-scene.config :refer [config]]
   [clojure.string :as str]
   [clojure.walk :as walk]
   [jme-clj.core :as jme]
   [kezban.core :as k]
   [mount.core :as mount :refer [defstate]])
  (:import (java.io ByteArrayOutputStream PrintStream)))


(defstate ^{:on-reload :noop}
  app
  :start (do
           (jme/defsimpleapp app*
             :opts {:show-settings? false
                    :pause-on-lost-focus? false
                    :display-stat-view? false
                    :display-fps? false
                    :settings {:title "3D Scene"
                               :load-defaults? true
                               :frame-rate 60
                               :width 800
                               :height 600
                               :resizable? true}}
             :init init
             :update simple-update
             :destroy #(when-not (:dev config)
                         (System/exit 0)))
           (jme/start app*))
  :stop (jme/unbind-all))


(defn- get-wrong-arity-fn-name [msg]
  (let [[_ v] (str/split msg #"passed to: ")]
    (name (symbol v))))


(defn- get-unresolved-var-name [msg]
  (let [[_ v] (str/split msg #"Unable to resolve symbol: | in this context")]
    v))


(defn- parse-error-msg [msg]
  (condp #(str/includes? %2 %1) msg

    "EOF while reading"
    (str "Looks like you did not close your parenthesis.\n"
         "Make sure that every opened form or function should be closed (my-function).\n"
         "These are closed forms: {}, #{}, [], ()")

    "Wrong number of args"
    (str "You called " (get-wrong-arity-fn-name msg) " with a wrong number of arguments.\n"
         "Please check your arguments.")

    "Unable to resolve symbol:"
    (str "It seems like you did not define " (get-unresolved-var-name msg) "\n"
         "You need to define it first then you can use it.")

    "cannot be cast to class clojure.lang.IFn"
    (str "You are trying to call a function, but you have invalid code.\n"
         "First argument has to be always a function, following forms are invalid;\n"
         "(12 \"Hello\") -> first argument is a number\n"
         "(\"Some string\") -> first argument is a string\n"
         "(true) -> first argument is a boolean")

    "Divide by zero" (str "Numbers can't be divided by zero. It is not acceptable in math :).")
    msg))


(defn- get-used-fns [code]
  (let [s (atom [])
        code (k/try-> code (#(str "(" % ")")) read-string)]
    (walk/prewalk (fn [form]
                    (when (list? form)
                      (swap! s conj form))
                    form)
                  code)
    (->> @s
         (filter (comp symbol? first))
         (map first)
         (set))))


(defmacro with-out [& body]
  `(let [err-buffer# (ByteArrayOutputStream.)
         original-err# System/err
         tmp-err# (PrintStream. err-buffer# true "UTF-8")
         out# (with-out-str (try
                              (System/setErr tmp-err#)
                              ~@body
                              (finally
                                (System/setErr original-err#))))]
     {:out out#
      :out-err (.toString err-buffer# "UTF-8")}))


;;TODO add timeout
(defn run [code]
  (try
    (let [forms (read-string (str "(" code ")"))
          result (atom {})
          p (promise)]
      (binding [jme/*app* app]
        (jme/enqueue (fn []
                       (let [out (with-out
                                   (try
                                     (eval (cons 'do forms))
                                     (catch Throwable t
                                       (swap! result assoc
                                              :error? true
                                              :error-msg (->> t Throwable->map :cause parse-error-msg)))))]
                         (swap! result merge out)
                         (deliver p true)))))
      (deref p)
      (assoc @result :used-fns (get-used-fns code)))
    (catch Throwable t
      {:error? true
       :error-msg (-> t Throwable->map :cause parse-error-msg)})))


(comment
  (jme/run app
           #_(create-box {:name "ertu"})
           #_(rotate {:name "ertu"
                      :degree 5
                      :axes :y})
           (scale {:name "ertu"
                   :scale 1.5}))

  (jme/run app (jme/re-init init))

  (do
    (mount/stop #'app)
    (mount/start #'app)))
