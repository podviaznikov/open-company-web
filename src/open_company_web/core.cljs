(ns ^:figwheel-always open-company-web.core
  (:require [om.core :as om :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]
            [open-company-web.router :as router]
            [open-company-web.components.page :refer [company]]
            [open-company-web.components.list-companies :refer [list-companies]]
            [open-company-web.components.page-not-found :refer [page-not-found]]
            [open-company-web.components.report :refer [report readonly-report]]
            [open-company-web.lib.raven :refer [raven-setup]]
            [open-company-web.dispatcher :refer [app-state]]
            [open-company-web.api :as api]
            [goog.events :as events]
            [accountant.core :as accountant])
  (:import [goog.history EventType]))

(enable-console-print!)

;; setup Sentry error reporting
(defonce raven (raven-setup))

; Routes - Do not define routes when js/document#app
; is undefined because it breaks tests
(if-let [target (. js/document (getElementById "app"))]
  (do
    (defroute list-page-route "/companies" []
      ; save route
      (router/set-route! ["companies"] {})
      ; load data from api
      (api/get-companies)
      ; render component
      (om/root list-companies app-state {:target target}))

    (defroute company-profile-route "/companies/:ticker" {{ticker :ticker} :params}
      ; save route
      (router/set-route! ["companies" ticker] {:ticker ticker})
      ; load data from api
      (api/get-company ticker)
      (swap! app-state assoc :loading true)
      ; render compoenent
      (om/root company app-state {:target target}))

    (defroute report-summary-route "/companies/:ticker/summary" {{ticker :ticker} :params}
      ; save route
      (router/set-route! ["companies" ticker "summary"] {:ticker ticker})
      ; load data from api
      (swap! app-state assoc :loading true)
      (api/get-company ticker)
      ; render component
      (om/root report app-state {:target target}))

    (defroute report-editable-route "/companies/:ticker/reports/:year/:period/edit" {{ticker :ticker year :year period :period} :params}
      ; save route
      (router/set-route! ["companies" ticker "reports" year period "edit"] {:ticker ticker :year year :period period})
      ; load data from api
      (swap! app-state assoc :loading true)
      (api/get-report ticker year period)
      ; render component
      (om/root report app-state {:target target}))

    (defroute report-route "/companies/:ticker/reports/:year/:period" {{ticker :ticker year :year period :period} :params}
      ; save route
      (router/set-route! ["companies" ticker "reports" year period] {:ticker ticker :year year :period period})
      ; load data from api
      (swap! app-state assoc :loading true)
      (api/get-report ticker year period)
      ; render component
      (om/root readonly-report app-state {:target target}))

    (defroute not-found-route "*" []
      ; render component
      (om/root page-not-found app-state {:target target}))

    (def dispatch!
      (secretary/uri-dispatcher [list-page-route
                                 company-profile-route
                                 report-summary-route
                                 report-editable-route
                                 report-route
                                 not-found-route]))

    (def route-locator!
      (secretary/route-locator [list-page-route
                                 company-profile-route
                                 report-summary-route
                                 report-editable-route
                                 report-route
                                 not-found-route]))

    ; initialize accountant to handle token changes and link clicks
    (accountant/configure-navigation! dispatch! route-locator!)
    ; dispatche the token
    (dispatch! (router/get-token))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (dispatch! (router/get-token))
)
