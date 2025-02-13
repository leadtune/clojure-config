(ns clojure-config.core
  (:require
	    [clojure.tools.logging :as log]
	    [clojure.walk :as w])
  (:import (java.net InetAddress))
  (:import (java.io File FileNotFoundException)))

(def ^{:dynamic true} *properties* {})



;; System Calls
(defn hostname []
  (let [addr (. InetAddress getLocalHost)]
    (.getHostName addr)))

(defn- env []
  (System/getenv "ENV"))

(defn- username []
  (System/getProperty "user.name"))


(defn- host-match? [param]
  (and (= (:type param) "host") (= (:value param) (hostname))))

(defn- user-match? [param]
  (and (= (:type param) "user") (= (:value param) (username))))

(defn- env-match? [param]
  (and (= (:type param) "env") (= (:value param) (env))))


(defn- get-property-files [profile]
  (let [value (:name profile)
	parent (:parent profile)]
    (let [ out
	  (if (not (nil? value))
	    (assoc {} :file (str value ".properties")))]
      (if (not (nil? parent))
	(assoc out :parent-file (str parent ".properties"))
	(assoc out :global "global.properties")))))


(defn- match-params? [current]
  (or (user-match? current)
      (host-match? current)
      (env-match? current)))

(defn- load-from-file [filename]
  (w/keywordize-keys (if (not (nil? filename))
    (let [resource (-> (Thread/currentThread)
		     (.getContextClassLoader)
		     (.getResource filename))]
    (if (not (nil? resource))
      (into {} (doto (java.util.Properties.)
		 (.load (-> (Thread/currentThread)
			    (.getContextClassLoader)
			    (.getResourceAsStream filename))))))))))


(defn- determine-profile [profiles]
  (first (filter match-params? profiles)))


(defn load-profile [profiles]
  (if (not (nil? profiles))
    (let [profile (determine-profile profiles)
	  global (first (filter (fn [x] (= (:name x) "global")) profiles))
	  parent (first (filter (fn [x] (= (:name x) (:parent profile))) profiles))
	  files (get-property-files profile)
	  properties (merge
		      (:properties global)
		      (:properties parent)
		      (:properties profile)
		      (load-from-file (:global files))
		      (load-from-file (:parent-file files))
		      (load-from-file (:file files)))]
          (log/debug (str "profile:" profile))
          (log/debug (str "global" global))
          (log/debug (str "parent:" parent))

      properties)))



(defn- load-property [key files]
  (let [file (load-from-file (:file files))
	parent (load-from-file (:parent-file files))]
    (if (nil? (get file key))
      (get parent key)
      (get file key))))


;; Public functions

(defn set-properties "Set the profile and loads the properties and binds *properties*" [profiles]
    (alter-var-root (var *properties*)
		    (constantly (load-profile profiles))))

(defn properties "Returns a map with all properties" []
  *properties*)

(defn property "Returns the value with the given key, or null" [key]
  (do
    ((keyword key) *properties*)))
