(ns ^{:defstate? true} backend-3d-scene.nrepl
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]
            [nrepl.server :as nrepl]))


(defn- start
  "Start a network repl for debugging on specified port followed by
   an optional parameters map. The :bind, :transport-fn, :handler,
   :ack-port and :greeting-fn will be forwarded to
   clojure.tools.nrepl.server/start-server as they are."
  [{:keys [port bind transport-fn handler ack-port greeting-fn]}]
  (try
    (log/info "starting nREPL server on port" port)
    (nrepl/start-server :port port
                        :bind bind
                        :transport-fn transport-fn
                        :handler handler
                        :ack-port ack-port
                        :greeting-fn greeting-fn)
    (catch Throwable t
      (log/error t "failed to start nREPL")
      (throw t))))


(defn- stop [server]
  (nrepl/stop-server server)
  (log/info "nREPL server stopped"))


(defstate ^{:on-reload :noop}
  repl-server
  :start (start {:port 3011})
  :stop (when repl-server
          (stop repl-server)))
