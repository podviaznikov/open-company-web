(ns open-company-web.components.cell
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om]
            [om-tools.core :as om-core :refer-macros [defcomponent]]
            [om-tools.dom :as dom :include-macros true]
            [open-company-web.lib.utils :as utils]
            [cljs.core.async :refer [chan <!]]))

;; Cell component
;; props: 
;; - :value an initial value for the component
;; - :placeholder an placeholder for the :new state
;; - :prefix a prefix to show before the formatte value
;; - :suffic a suffix to show after the formatte value
;; - :cell-state (optional) an initial state for the component


(defn- format-value [value]
  (.toLocaleString value))

(defn- to-state [owner data state]
  (when (or (= state :draft) (= state :display) (= state :new))
    ((:draft-cb data) (.parseFloat js/window (.. (om/get-ref owner "edit-field") getDOMNode -value))))
  (om/update-state! owner :cell-state (fn [_] state))
  (when (= state :edit)
    (.setTimeout js/window #(let [input (om/get-ref owner "edit-field")]
                              (when input
                                (.focus (.getDOMNode input)))) 10)))

(defn- initial-cell-state
  "Get the initial state for the cell.
   If :value is empty (ie 0 length or nil/undefined) it returns :new
   else it returns :display"
  [data]
  (let [value (:value data)
        state (:cell-state data)]
    (or state (if (>= value 0)
      :display
      :new))))

(defn exit-cell [e owner data]
  (let [value (.parseFloat js/window (.. e -target -value))
        init-value (om/get-state owner :inital-value)
        ; if the value is the same as it was at the start
        ; go to the :display state, else go to :draft
        state (if (= value init-value) :display :draft)
        ; if the value is empty and it was empty got to the :new state
        state (if (and (= state :display) (= value "")) :new state)]
    (to-state owner data state)))

(defcomponent cell [data owner]
  (init-state [_]
    (utils/add-channel (str (:period data) (:key data)) (chan))
    (let [parsed-value (.parseFloat js/window (:value data))
          value (if (js/isNaN parsed-value) "" parsed-value)]
      {:cell-state (initial-cell-state data)
       :inital-value (:value data)
       :value value}))
  (did-mount [_]
    ; initialize tooltips only if jquery is loaded, avoid tests crash
    (when (.-$ js/window)
      (.tooltip (.$ js/window "[data-toggle=\"tooltip\"]")))
    (go (loop []
      (let [ch (utils/get-channel (str (:period data) (:key data)))
            signal (<! ch)]
        (.setTimeout js/window #(to-state owner data :edit) 100)
        (recur)))))
  (render [_]
    (let [value (om/get-state owner :value)
          float-value (.parseFloat js/window value)
          float-value (if (js/isNaN float-value) 0 float-value)
          formatted-value (format-value float-value)
          prefix-value (if (:prefix data) (str (:prefix data) formatted-value) formatted-value)
          final-value (if (:suffix data) (str (:suffix data) prefix-value) prefix-value)
          state (om/get-state owner :cell-state)
          tooltip-text (case state
                        :new      "Click to enter data"
                        :display  "Click to edit data"
                        :draft    "Click to modify data"
                        :edit     "Press enter")]
      (dom/div {:class "comp-cell"
                :data-placement "top"
                :data-toggle "tooltip"
                :title tooltip-text}
        (case state

          :new
          (dom/div {:class "comp-cell-int state-new"
                    :on-click #(to-state owner data :edit)}
            (dom/span {} (:placeholder data)))

          :display
          (dom/div {:class "comp-cell-int state-display"
                    :on-click #(to-state owner data :edit)}
            (dom/span {} final-value))

          :draft
          (dom/div {:class "comp-cell-int state-draft"
                    :on-click #(to-state owner data :edit)}
            (dom/span {} final-value))

          :edit
          (dom/div {:class "comp-cell-int state-edit"}
            (dom/input #js {
                       :ref "edit-field"
                       :value value
                       :onFocus #(let [input (.getDOMNode (om/get-ref owner "edit-field"))]
                                   (set! (.-value input) (.-value input)))
                       :onChange #(om/update-state! owner :value (fn [_] (.. % -target -value)))
                       :onBlur #(exit-cell % owner data)
                       :onKeyDown #(cond

                                     (= "Enter" (.-key %))
                                     (exit-cell % owner data)

                                     (= "Tab" (.-key %))
                                     (do
                                       (exit-cell % owner data)
                                       ((:tab-cb data) (:period data) (:key data))
                                       (.preventDefault %)))})))))))