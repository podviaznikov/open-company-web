(ns open-company-web.components.sidebar
    (:require [om.core :as om :include-macros true]
              [om-tools.core :as om-core :refer-macros [defcomponent]]
              [om-tools.dom :as dom :include-macros true]
              [open-company-web.components.link :refer [link]]
              [open-company-web.router :as router]))

(defcomponent sidebar [data owner]
  (init-state [_]
    {:path @router/path})
  (render [_]
    (let [path (om/get-state owner :path)
          ticker (:ticker path)
          profile-url (str "/companies/" ticker)
          organization-url (str "/companies/" ticker "/organization")
          equity-url (str "/companies/" ticker "/equity")
          agreements-url (str "/companies/" ticker "/agreements")
          reports-url (str "/companies/" ticker "/summary")
          is-profile (= (:active data) "profile")
          is-organization (= (:active data) "organization")
          is-equity (= (:active data) "equity")
          is-agreements (= (:active data) "agreements")
          is-report (= (:active data) "reports")]
      (dom/div {:class "col-mid-1 sidebar"}
        (dom/ul {:class "nav nav-sidebar"}
          ; profile
          (dom/li {:class (if is-profile "active" "")}
            (dom/a {:href profile-url :alt "Profile"} "Profile"))
          ; organization
          (dom/li {:class (if is-organization "active" "")}
            (dom/a {:href organization-url :alt "Organization"} "Organization"))
          ; equity
          (dom/li {:class (if is-equity "active" "")}
            (dom/a {:href equity-url :alt "Equity"} "Equity"))
          ; agreements
          (dom/li {:class (if is-agreements "active" "")}
            (dom/a {:href agreements-url :alt "Agreements"} "Agreements"))
          ; reports
          (dom/li {:class (if is-report "active" "")}
            (dom/a {:href reports-url :alt "Reports"} "Reports"))
          (dom/li {:class ""}
            (dom/a {:href "http://localhost:3449/test" :alt "Reports"} "Reports")))))))