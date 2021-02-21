(ns backend-3d-scene.core
  (:require
   [backend-3d-scene.nrepl :as nrepl]
   [clojure.string :as str]
   [mount.core :as mount])
  (:gen-class))


(defn- get-wrong-arity-fn-name [msg]
  (let [[_ v] (str/split msg #"passed to: ")]
    (name (symbol v))))


(defn- get-unresolved-var-name [msg]
  (let [[_ v] (str/split msg #"Unable to resolve symbol: | in this context")]
    v))


(defn parse-error-msg [msg]
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


(comment
  (try
   ;(eval '(selam 1))
   ;(read-string "(")
   ;(/ 2 0)
    ;(eval (read-string "(12 12)"))
    (catch Exception e
      (parse-error-msg (:cause (Throwable->map e)))
     ;(:cause (Throwable->map e))
      )))


(defn -main [& args]
  (mount/start #'nrepl/repl-server)
  (println args))
