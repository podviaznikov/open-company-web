(ns open-company-web.components.finances.burn-rate
  (:require [om.core :as om :include-macros true]
            [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [open-company-web.router :as router]
            [open-company-web.lib.utils :as utils]
            [open-company-web.lib.iso4217 :refer [iso4217]]
            [open-company-web.components.charts :refer [column-chart]]
            [open-company-web.components.finances.utils :as finances-utils]
            [open-company-web.components.utility-components :refer [editable-pen]]))

(defcomponent burn-rate [data owner]
  (render [_]
    (let [finances-data (:data (:section-data data))
          value-set (first finances-data)
          period (utils/period-string (:period value-set))
          currency (finances-utils/get-currency-for-current-company)
          cur-symbol (utils/get-symbol-for-currency-code currency)
          burn-rate-val (str cur-symbol (utils/format-value (:burn-rate value-set)))]
      (dom/div {:class (utils/class-set {:section true
                                         :burn-rate true
                                         :read-only (:read-only data)})}
        (om/build column-chart (finances-utils/get-chart-data finances-data
                                                              cur-symbol
                                                              :burn-rate
                                                              "Burn Rate"
                                                              #js {"type" "string" "role" "style"}
                                                              "fill-color: #0266C8"))
        (dom/h3 {}
                burn-rate-val
                (om/build editable-pen {:click-callback (:editable-click-callback data)}))
        (dom/p {} period)))))