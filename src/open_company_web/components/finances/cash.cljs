(ns open-company-web.components.finances.cash
  (:require [om.core :as om :include-macros true]
            [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [open-company-web.router :as router]
            [open-company-web.lib.utils :as utils]
            [open-company-web.lib.iso4217 :refer [iso4217]]
            [open-company-web.components.charts :refer [column-chart]]
            [open-company-web.components.finances.utils :as finances-utils]
            [open-company-web.components.utility-components :refer [editable-pen]]))

(defcomponent cash [data owner]
  (render [_]
    (let [finances-data (:data (:section-data data))
          sort-pred (utils/sort-by-key-pred :period true)
          sorted-finances (sort #(sort-pred %1 %2) finances-data)
          value-set (first sorted-finances)
          period (utils/period-string (:period value-set))
          currency (finances-utils/get-currency-for-current-company)
          cur-symbol (utils/get-symbol-for-currency-code currency)
          cash-val (str cur-symbol (utils/format-value (:cash value-set)))]
      (dom/div {:class (utils/class-set {:section true
                                         :cash true
                                         :read-only (:read-only data)})}
        (om/build column-chart (finances-utils/get-chart-data sorted-finances
                                                              cur-symbol
                                                              :cash
                                                              "Cash"
                                                              #js {"type" "string" "role" "style"}
                                                              "fill-color: #ADADAD"))
        (dom/h3 {}
                cash-val
                (om/build editable-pen {:click-callback (:editable-click-callback data)}))
        (dom/p {} period)))))