(ns open-company-web.components.charts
  (:require [om.core :as om :include-macros true]
            [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]))

(defn pie-add-rows [columns data currency-symbol]
  (let [data-table (js/google.visualization.DataTable.)
        formatter (js/google.visualization.NumberFormat. #js {
                    "negativeColor" "red",
                    "negativeParens" true
                    "pattern" "###,###.##"
                    "prefix" (if (= currency-symbol "%") "" currency-symbol)
                    "suffix" (if (not (= currency-symbol "%")) "" "%")})]
    (doseq [x columns]
      (.addColumn data-table (first x) (second x)))
    (.addRows data-table (clj->js data))
    (.format formatter data-table 1)
    data-table))

(defn pie-draw-chart [currency-symbol columns data dom-node]
  (when (.-google js/window)
    (let [data-table (pie-add-rows columns data currency-symbol)
          options (clj->js {
                    :title  ""
                    :width 600
                    :height 250})]
      (when dom-node (.draw (js/google.visualization.PieChart. dom-node) data-table options)))))

(defcomponent pie-chart [chart-data owner]
  (did-mount [_]
    (pie-draw-chart (:prefix chart-data)
                    (:columns chart-data)
                    (:values chart-data)
                    (.getDOMNode (om/get-ref owner "pie-chart"))))
  (did-update [_ _ _]
    (pie-draw-chart (:prefix chart-data)
                    (:columns chart-data)
                    (:values chart-data)
                    (.getDOMNode (om/get-ref owner "pie-chart"))))
  (render [_]
    (dom/div #js {:className "chart-container pie-chart" :ref "pie-chart" })))

(defn column-add-rows [columns data currency-symbol pattern]
  (let [data-table (js/google.visualization.DataTable.)
        formatter (js/google.visualization.NumberFormat. #js {
                    "negativeColor" "red",
                    "negativeParens" true
                    "pattern" pattern ;"###,###.##"
                    "prefix" (if (= currency-symbol "%") "" currency-symbol)
                    "suffix" (if-not (= currency-symbol "%") "" "%")})]
    (doseq [x columns]
      (if (vector? x)
        (.addColumn data-table (first x) (second x))
        (.addColumn data-table x)))
    (.addRows data-table (clj->js data))
    (doseq [idx (range (count columns))]
      (let [column (columns idx)]
        (when (and (vector? column) (= (first column) "number"))
          (.format formatter data-table idx))))
    data-table))

(defn column-draw-chart [currency-symbol columns pattern data column-thickness dom-node]
  (when (.-google js/window)
    (let [data-table (column-add-rows columns data currency-symbol pattern)
          options (clj->js {
                    :title  ""
                    :width 600
                    :height 290
                    :legend #js {"position" "none"}
                    :vAxis #js {"minValue" 0
                                "gridlineColor" "transparent"
                                "baselineColor" "transparent"
                                "textPosition" "none"}
                    :hAxis #js {"textStyle" #js {"fontSize" 9}}
                    :chartArea #js {"left" 0 "top" 30 "width" "100%" "height" "80%"}
                    :bar #js { "groupWidth" column-thickness}})]
      (when dom-node (.draw (js/google.visualization.ColumnChart. dom-node) data-table options)))))

(defcomponent column-chart [chart-data owner]
  (did-mount [_]
    (column-draw-chart (:prefix chart-data)
                       (:columns chart-data)
                       (:pattern chart-data)
                       (:values chart-data)
                       (if (contains? chart-data :column-thickness)
                         (:column-thickness chart-data)
                         "10%")
                       (.getDOMNode (om/get-ref owner "column-chart"))))
  (did-update [_ _ _]
    (column-draw-chart (:prefix chart-data)
                       (:columns chart-data)
                       (:pattern chart-data)
                       (:values chart-data)
                       (if (contains? chart-data :column-thickness)
                         (:column-thickness chart-data)
                         "10%")
                       (.getDOMNode (om/get-ref owner "column-chart"))))
  (render [_]
    (dom/div #js {:className "chart-container column-chart" :ref "column-chart" })))