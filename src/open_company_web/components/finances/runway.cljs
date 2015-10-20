(ns open-company-web.components.finances.runway
  (:require [om.core :as om :include-macros true]
            [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [open-company-web.router :as router]
            [open-company-web.lib.utils :as utils]
            [open-company-web.lib.iso4217 :refer [iso4217]]
            [open-company-web.components.charts :refer [column-chart]]
            [open-company-web.components.finances.utils :refer [get-chart-data]]
            [open-company-web.components.utility-components :refer [editable-pen]]))

(defcomponent runway [data owner]
  (render [_]
    (let [finances-data (:data (:finances (:company-data data)))
          value-set (first finances-data)
          runway-val (utils/format-value (:runway value-set))
          period (utils/period-string (:period value-set))
          cur-symbol (utils/get-symbol-for-currency-code (:currency data))]
      (dom/div {:class (str "section runway" (when (:read-only data) " read-only"))}
        (dom/h3 {}
                runway-val
                (om/build editable-pen {:click-callback (:editable-click-callback data)}))
        (dom/p {} period)
        (om/build column-chart (get-chart-data finances-data "" :runway "runway"))))))