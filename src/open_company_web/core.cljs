(ns ^:figwheel-always open-company-web.core
  (:require [om.core :as om :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]
            [open-company-web.router :as router]
            [open-company-web.components.page :refer [company company-profile-container]]
            [open-company-web.components.list-companies :refer [list-companies]]
            [open-company-web.components.page-not-found :refer [page-not-found]]
            [open-company-web.components.user-profile :refer [user-profile]]
            [open-company-web.components.login :refer [login]]
            [open-company-web.lib.raven :refer [raven-setup]]
            [open-company-web.lib.utils :as utils]
            [open-company-web.dispatcher :refer [app-state]]
            [open-company-web.api :as api]
            [goog.events :as events]
            [open-company-web.lib.cookies :as cook]
            [open-company-web.local-settings :as ls]
            [open-company-web.lib.jwt :as jwt]
            [accountant.core :as accountant])
  (:import [goog.history EventType]))

(enable-console-print!)

;; setup Sentry error reporting
(defonce raven (raven-setup))

; Routes - Do not define routes when js/document#app
; is undefined because it breaks tests
(if-let [target (. js/document (getElementById "app"))]
  (do

    (defn login-routes-handler [query-params]
      (if (contains? query-params :jwt)
        (do ; contains :jwt so auth went well
          (cook/set-cookie! :jwt (:jwt query-params) (* 60 60 24 60) "/" ls/jwt-cookie-domain ls/jwt-cookie-secure)
          ;redirect to dashboard
          (if-let [login-redirect (cook/get-cookie :login-redirect)]
            (do
              ; remove the login redirect cookie
              (cook/remove-cookie! :login-redirect)
              ; redirect to the initial path
              (utils/redirect! login-redirect))
            ; redirect to / if no cookie is set
            (utils/redirect! "/")))
        (do
          (when (contains? query-params :login-redirect)
            (cook/set-cookie! :login-redirect (:login-redirect query-params)))
          ; save route
          (router/set-route! ["login"] {})
          ; load data from api
          (swap! app-state assoc :loading true)
          (api/get-auth-settings)
          (when (contains? query-params :access)
            ;login went bad, add the error message to the app-state
            (swap! app-state assoc :access (:access query-params)))
          ; render component
          (om/root login app-state {:target target}))))

    (defroute login-route ((:login router/routes) 0) {:keys [query-params]}
      (login-routes-handler query-params))

    (defroute login-route-slash ((:login router/routes) 1) {:keys [query-params]}
      (login-routes-handler query-params))

    (defn login-wall []
      (let [token (router/get-token)]
        (when-not (.startsWith token "/login")
          (if (cook/get-cookie :jwt)
            true
            (utils/redirect! (str "/login?login-redirect=" (router/get-token)))))))

    (defn home-routes-handler []
      (login-wall)
      ; save route
      (router/set-route! ["companies"] {})
      ; load data from api
      (api/get-companies)
      ; render component
      (om/root list-companies app-state {:target target}))

    (defroute home-page-route ((:home router/routes) 0) []
      (home-routes-handler))

    (defroute home-page-route-slash ((:home router/routes) 1) []
      (home-routes-handler))

    (defroute list-page-route ((:home router/routes) 2) []
      (home-routes-handler))

    (defroute list-page-route-slash ((:home router/routes) 3) []
      (home-routes-handler))

    (defn company-profile-routes-handler [params]
      (login-wall)
      (let [slug (:slug (:params params))
            query-params (:query-params params)]
        ; save route
        (router/set-route! ["companies" slug "profile"] {:slug slug :query-params query-params})
        ; load data from api
        (api/get-company slug)
        (swap! app-state assoc :loading true)
        ; render compoenent
        (om/root company-profile-container app-state {:target target})))

    (defroute company-profile-route ((:company-profile router/routes) 0) {:as params}
      (company-profile-routes-handler params))

    (defroute company-profile-route-slash ((:company-profile router/routes) 1) {:as params}
      (company-profile-routes-handler params))

    (defn company-routes-handler [params]
      (login-wall)
      (let [slug (:slug (:params params))
            query-params (:query-params params)]
        ; save route
        (router/set-route! ["companies" slug] {:slug slug
                                               :query-params query-params})
        (when-not (contains? @app-state (keyword slug))
          ; load data from api
          (api/get-company slug)
          (swap! app-state assoc :loading true))
        ; render compoenent
        (om/root company app-state {:target target})))

    (defroute company-route ((:company router/routes) 0) {:as params}
      (company-routes-handler params))

    (defroute company-route-slash ((:company router/routes) 1) {:as params}
      (company-routes-handler params))

    (defn section-routes-handler [params]
      (login-wall)
      (let [slug (:slug (:params params))
            section (:section (:params params))
            query-params (:query-params params)]
        ; save route
        (router/set-route! ["companies" slug section] {:slug slug
                                                       :section section
                                                       :query-params query-params})
        ; if there are no company data
        (when-not (contains? @app-state (keyword slug))
          ; load data from api
          (swap! app-state assoc :loading true)
          (api/get-company slug))
        ; render component
        (om/root company app-state {:target target})))

    (defroute section-route ((:section router/routes) 0) {:as params}
      (section-routes-handler params))

    (defroute section-route-slash ((:section router/routes) 1) {:as params}
      (section-routes-handler params))

    (defn user-profile-routes-handler [params]
      (login-wall)
      (om/root user-profile app-state {:target target}))

    (defroute user-profile-route ((:profile router/routes) 0) {:as params}
      (user-profile-routes-handler params))

    (defroute user-profile-route-slash ((:profile router/routes) 1) {:as params}
      (user-profile-routes-handler params))

    (defroute not-found-route "*" []
      (do
        (login-wall)
        ; render component
        (om/root page-not-found app-state {:target target})))

    (def routes-stack
      [login-route
       login-route-slash
       home-page-route
       home-page-route-slash
       list-page-route
       list-page-route-slash
       company-route
       company-route-slash
       company-profile-route
       company-profile-route-slash
       section-route
       section-route-slash
       user-profile-route
       user-profile-route-slash
       not-found-route])

    (def dispatch!
      (secretary/uri-dispatcher routes-stack))

    (def routes-list
      (vec (flatten (map #(% router/routes) (keys router/routes)))))

    ; initialize accountant to handle token changes and link clicks
    (accountant/configure-navigation! dispatch! routes-list)
    ; dispatche the token
    (dispatch! (router/get-token))))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (.clear js/console)
  (dispatch! (router/get-token))
)
