(ns open-company-web.router
  (:require [secretary.core :as secretary])
  (:import [goog.history Html5History]))

(enable-console-print!)

(def path (atom {}))

(defn set-route! [route parts]
  (swap! path assoc :route route)
  (doseq [[k v] parts] (swap! path assoc k v)))

(defn get-token []
  (str js/window.location.pathname js/window.location.search))