(ns backend-3d-scene.api
  (:refer-clojure :exclude [print println])
  (:require
   [clojure.core.async :as a]
   [clojure.string :as str]
   [clojure.walk :as walk]
   [jme-clj.core :as jme]
   [kezban.core :as k]
   [mount.core :as mount :refer [defstate]])
  (:import (com.jme3.math ColorRGBA)
           (java.io StringWriter)))

(defonce out-ch (a/chan))


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
    (str "You called " (get-wrong-arity-fn-name msg) " with a wrong number of parameters.\n"
         "Please check your parameters.")

    "Unable to resolve symbol:"
    (str "It seems like you did not define " (get-unresolved-var-name msg) "\n"
         "You need to define it first then you can use it.")

    "cannot be cast to class clojure.lang.IFn"
    (str "You are trying to call a function, but you have invalid code.\n"
         "First parameter has to be always a function, following forms are invalid;\n"
         "(12 \"Hello\") -> first parameter is a number\n"
         "(\"Some string\") -> first parameter is a string\n"
         "(true) -> first parameter is a boolean")

    "Divide by zero" (str "Numbers can't be divided by zero. It is not acceptable in math :).")
    msg))


(defn- get-used-fns [code]
  (let [s (atom [])
        code (k/try-> code (#(str "(" % ")")) (read-string))]
    (walk/prewalk (fn [form]
                    (when (list? form)
                      (swap! s conj form))
                    form)
                  code)
    (->> @s
         (filter (comp symbol? first))
         (map first))))


(defn- init []
  (let [box (jme/box 1 1 1)
        geom (jme/geo "Box" box)
        mat (jme/material "Common/MatDefs/Misc/Unshaded.j3md")]
    ;(jme/set* (jme/input-manager) :cursor-visible true)
    ;(jme/set* (jme/fly-cam) :enabled false)
    (jme/set* mat :color "Color" ColorRGBA/Blue)
    (jme/set* geom :material mat)
    (jme/add-to-root geom)))


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
                               :resizable? false}}
             :init init)
           (jme/start app*))
  :stop (jme/unbind-app #'app*))


(defn print [& more]
  (let [s (StringWriter.)]
    (binding [*print-readably* nil
              *out* s]
      (apply pr more)
      (a/put! out-ch (str s))
      nil)))


(defn println [& more]
  (let [s (StringWriter.)]
    (binding [*print-readably* nil
              *out* s]
      (apply prn more)
      (a/put! out-ch (str s))
      nil)))


(defn run [code]
  (try
    (let [forms (read-string (str "(" code ")"))]
      (binding [jme/*app* app]
        (jme/enqueue (fn []
                       (try
                         (eval (cons 'do forms))
                         (catch Throwable t
                           (->> t Throwable->map :cause parse-error-msg (hash-map :error) (a/put! out-ch)))))))
      {:used-fns (get-used-fns code)})
    (catch Throwable t
      {:error? true
       :error-msg (-> t Throwable->map :cause parse-error-msg)})))


(comment
  (println "hey")
  (a/go-loop []
    (let [x (a/<! out-ch)]
      (clojure.core/println x))
    (recur))
  (macroexpand-1 '(run "(print 'selam 2"))
  (run "(println \"Ertu\") (/ 2 0)")
  (do
    (mount/stop #'app)
    (mount/start #'app)))