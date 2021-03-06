(ns test.open-company-web.components.update-footer
    (:require [cljs.test :refer-macros [deftest async testing is are use-fixtures]]
              [cljs-react-test.simulate :as sim]
              [cljs-react-test.utils :as tu]
              [om.core :as om :include-macros true]
              [dommy.core :as dommy :refer-macros [sel1 sel]]
              [open-company-web.components.update-footer :refer [update-footer]]
              [om.dom :as dom :include-macros true]
              [open-company-web.lib.utils :as utils]))

(enable-console-print!)

; dynamic mount point for components
(def ^:dynamic c)

(def test-atom {
  :updated-at "2015-09-14T20:49:19Z"
  :section "update"
  :author {
    :name "Stuart Levinson"
    :user-id "U06SQLDFT"
    :image "https://avatars.slack-edge.com/2015-10-16/12647678369_79b4fbf15439d29d5457_192.jpg"
  }
})


(deftest test-update-footer-component
  (testing "Update footer component"
    (let [c (tu/new-container!)
          app-state (atom test-atom)
          _ (om/root update-footer app-state {:target c})
          timeago-node (sel1 c [:div.timeago])
          author-node (sel1 c [:div.author])
          image-node (sel1 c [:img.author-image])]
      (is (not (nil? timeago-node)))
      (is (not (nil? author-node)))
      (is (not (nil? image-node)))
      (tu/unmount! c))))