(defproject backend-3d-scene "0.1.0-SNAPSHOT"

  :description "FIXME: write description"

  :url "http://example.com/FIXME"

  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/tools.logging "1.1.0"]
                 [jme-clj "0.1.13"]
                 [mount "0.1.16"]
                 [nrepl "0.8.3"]]

  :plugins [[ertu/lein-bikeshed "0.1.13"]
            [ertu/lein-carve "0.1.0"]
            [jonase/eastwood "0.3.11"]
            [lein-ancient "0.6.15"]
            [lein-cljfmt "0.7.0"]
            [lein-nsort "0.1.14"]
            [pisano/lein-kibit "0.1.2"]]

  :min-lein-version "2.0.0"

  :repl-options {:init-ns backend-3d-scene.core}

  :main backend-3d-scene.core

  :aot :all

  :omit-source true

  :uberjar-name "backend-3d-scene.jar"

  :cljfmt {:remove-consecutive-blank-lines? false}

  :eastwood {:source-paths ["src"]
             :exclude-namespaces [:test-paths]
             :add-linters [:unused-private-vars]
             :exclude-linters [:deprecations
                               :implicit-dependencies
                               :unused-ret-vals
                               :unused-meta-on-macro
                               :local-shadows-var
                               :constant-test]}

  :nsort {:require :alias-bottom-asc
          :source-paths ["src" "test"]}

  :bikeshed {:max-line-length 120
             :source-paths ["src" "test"]}

  :carve {:paths ["src" "test"]
          :dry-run true
          :report {:format :text}}

  :aliases {"lint" ["do"
                    ["cljfmt" "check"]
                    ["nsort"]
                    ["bikeshed"]
                    ["carve"]
                    ["clj-kondo" "--lint" "src"]
                    ["kibit"]
                    ["eastwood"]]
            "clj-kondo" ["with-profile" "+dev" "run" "-m" "clj-kondo.main"]}

  :jvm-opts ^:replace ["-XX:-OmitStackTraceInFastThrow"
                       "-XX:+ScavengeBeforeFullGC"
                       "-XX:+IgnoreUnrecognizedVMOptions"
                       "-Djava.net.preferIPv4Stack=true"
                       "-Dfile.encoding=UTF-8"]

  :profiles {:dev {:dependencies [[clj-kondo "2021.02.13"]
                                  [org.clojure/tools.logging "1.1.0"]]
                   :repl-options {:init-ns jme-clj.core}
                   :resource-paths ["test/resources"]}})
