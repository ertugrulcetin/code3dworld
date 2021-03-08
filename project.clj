(defproject code3dworld "0.1.0-SNAPSHOT"

  :description "Code 3D World is interactive, and batteries included a 3D coding platform."

  :url "https://github.com/ertugrulcetin/code3dworld"

  :author "Ertuğrul Çetin and Burkay Durdu"

  :license {:name "MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.11.7"]
                 [reagent "0.10.0"]
                 [re-frame "1.1.2"]
                 [day8.re-frame/tracing "0.6.0"]]

  :plugins [[lein-shadow "0.3.1"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-cljfmt "0.7.0"]
            [lein-nsort "0.1.14"]
            [lein-less "1.7.5"]]

  :less {:source-paths ["resources/public/less"]
         :target-path "resources/public/css"}

  :min-lein-version "2.9.0"

  :source-paths ["src"]

  :nsort {:require {:sort-fn (juxt (comp (complement string?) first) #(.indexOf % :as) first)
                    :comp #(compare %2 %1)}
          :source-paths ["src"]}

  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljfmt {:remove-consecutive-blank-lines? false}

  :shadow-cljs {:nrepl {:port 8777}

                :dev-http {8080 "resources/public/"}

                :builds {:main {:target :node-script
                                :output-to "resources/main.js"
                                :main code3dworld.main.core/main}
                         :renderer {:target :browser
                                    :output-dir "resources/public/js"
                                    :asset-path "/js"
                                    :modules {:renderer {:init-fn code3dworld.renderer.core/init
                                                         :preloads [devtools.preload
                                                                    day8.re-frame-10x.preload]}}
                                    :dev {:compiler-options {:closure-defines {re-frame.trace.trace-enabled? true
                                                                               day8.re-frame.tracing.trace-enabled? true}}}
                                    :release {:build-options
                                              {:ns-aliases
                                               {day8.re-frame.tracing day8.re-frame.tracing-stubs}}}

                                    :devtools {:http-root "resources/public"
                                               :http-port 8280}}}}


  :shell {:commands {"karma" {:windows ["cmd" "/c" "karma"]
                              :default-command "karma"}
                     "open" {:windows ["cmd" "/c" "start"]
                             :macosx "open"
                             :linux "xdg-open"}}}

  :aliases {"dev" ["do"
                   ["shell" "echo" "\"DEPRECATED: Please use lein watch instead.\""]
                   ["watch"]]

            "watch" ["with-profile" "dev" "do"
                     ["shadow" "watch" "main" "renderer" "browser-test" "karma-test"]]

            "prod" ["do"
                    ["shell" "echo" "\"DEPRECATED: Please use lein release instead.\""]
                    ["release"]]

            "release" ["with-profile" "prod" "do"
                       ;; added main here, I hope it works...
                       ["shadow" "release" "main" "renderer"]]

            "compile" ["with-profile" "prod" "do"
                       ["shadow" "compile" "main" "renderer"]]

            "build-report" ["with-profile" "prod" "do"
                            ["shadow" "run" "shadow.cljs.build-report" "main" "renderer" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]

            "karma" ["do"
                     ["shell" "echo" "\"DEPRECATED: Please use lein ci instead.\""]
                     ["ci"]]

            "ci" ["with-profile" "prod" "do"
                  ["shadow" "compile" "karma-test"]
                  ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]

            "lint" ["do"
                    ["nsort"]
                    ["cljfmt" "check"]
                    ["clj-kondo" "--lint" "src"]]

            "clj-kondo" ["with-profile" "+dev" "run" "-m" "clj-kondo.main"]

            "fix" ["do"
                   ["nsort" "-r"]
                   ["cljfmt" "fix"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "1.0.2"]
                   [day8.re-frame/re-frame-10x "0.7.0"]
                   [clj-kondo "2021.02.13"]]
    :source-paths ["dev"]}

   :prod {}}



  :prep-tasks [])
