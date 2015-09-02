(ns open-company-web.components.user-selector
  (:require [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [open-company-web.lib.utils :as utils]
            [cljs-dynamic-resources.core :as cdr]))

(defn get-name [user]
  (let [real-name (:real_name user)]
    (if (> (count real-name) 0)
      real-name
      (:name user))))

(defn format-state [state]
  (if (not (.-id state)) (.-text state))
  (let [text (.-text state)
        el (.$ js/window (.-element state))
        img (.data el "icon")
        my-temp (.$ js/window (str "<span><img class=\"user-icon\" src=\"" img "\" />" (.-text state) "</span>"))]
    my-temp))

(defcomponent user-selector [data owner]
  (will-mount [_]
    (cdr/add-script! "//ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js")
    (cdr/add-script! "/lib/select2/js/select2.js")
    (cdr/add-style! "/lib/select2/css/select2.css"))
  (did-mount [_]
    ; async initialize the user selector widget
    (.setTimeout js/window (fn []
                             (let [us (.$ js/window ".user-selector")]
                               (.select2 us (clj->js {"templateResult" format-state
                                                      "templateSelection" format-state}))))
                 1000))
  (render [_]
    (dom/div {:class "col-md-4"}
      (dom/select {:class "user-selector"
                   :value (:value data)
                   :style {"width" "100%"}}
        (for [user (:users data)]
          (dom/option {
                       :value (:id user)
                       :disabled (:is_bot user)
                       :data-icon (:image_24 (:profile user))}
                      (get-name user)))))))
