(ns open-company-web.components.finances.utils
  (:require [open-company-web.lib.utils :as utils]))

(def columns 7)

(defn chart-data-at-index [data keyw idx]
  (let [rev-idx (- (- (min (count data) columns) 1) idx)
        obj (get data rev-idx)]
    [(utils/period-string (:period obj)) (keyw obj)]))

(defn- get-chart-data [data prefix keyw column-name]
  "Vector of max *columns elements of [:Label value]"
  (let [chart-data (partial chart-data-at-index data keyw)
        placeholder-vect (subvec [0 1 2 3 4 5 6] 0 (min (count data) columns))]
    { :prefix prefix
      :columns [["string" column-name] ["number" (utils/camel-case-str (name keyw))]]
      :values (into [] (map chart-data placeholder-vect))}))