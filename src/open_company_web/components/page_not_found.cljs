(ns open-company-web.components.page-not-found
  (:require [om.core :as om :include-macros true]
            [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [open-company-web.components.link :refer [link]]))

(defcomponent page-not-found [data owner]
  (render [_]
    (dom/div
      (dom/h1 "Page not found")
      (dom/a {:href "/companies" :alt "Home"} "Home"))))