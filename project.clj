(defproject com.maitria/packthread "0.1.4-SNAPSHOT"
  :description "Threading macros for working with globs of state"
  :url "https://github.com/maitria/packthread"
  :license {:name "avi license"
            :url "http://github.com/maitria/avi/README.md"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.match "0.2.1"]]
  :profiles {:dev {:dependencies [[midje "1.6.3"]]
                   :plugins [[lein-midje "3.1.1"]
                             [net.eraserhead.clojure/lein-release "1.0.6"]]}}
  :lein-release {:scm :git,
                 :deploy-via :clojars,
                 :tag-format ["v" :version]})
