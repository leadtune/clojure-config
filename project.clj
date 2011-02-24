(defproject clojure-config "1.0.5"
  :description "A simple clojure framework for loading property configurations runtime"

  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]]

  
  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}

  :dev-dependencies [[swank-clojure "1.2.1"]
		     [com.stuartsierra/lazytest "1.1.2"]
		     [lein-lazytest "1.0.1"]]

  :lazytest-path ["src" "test"])
