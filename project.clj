(defproject com.maitria/packthread "0.1.11-SNAPSHOT"
  :description "Threading macros for working with globs of state"
  :url "https://github.com/maitria/packthread"
  :license {:name "avi license"
            :url "http://github.com/maitria/avi/README.md"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.match "0.2.1"]]
  :profiles {:dev {:dependencies [[midje "1.9.1"]]
                   :plugins [[lein-midje "3.1.1"]]}}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "v"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
