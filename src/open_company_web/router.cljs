(ns open-company-web.router)

(def path (atom {}))

(defn set-route! [route parts]
  (reset! path {})
  (swap! path assoc :route route)
  (doseq [[k v] parts] (swap! path assoc k v)))

(defn get-token []
  (str js/window.location.pathname js/window.location.search))

(def routes {
  :login ["/login" "/login/"]
  :home ["" "/" "/companies" "/companies/"]
  :company-profile ["/companies/:slug/profile" "/companies/:slug/profile/"]
  :company ["/companies/:slug" "/companies/:slug/"]
  :section ["/companies/:slug/:section" "/companies/:slug/:section/"]
  :profile ["/profile" "/profile/"]})