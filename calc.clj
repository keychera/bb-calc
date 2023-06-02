(ns calc
  (:require [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [common :refer [payload->map]]
            [hiccup2.core :refer [html]]
            [selmer.parser :refer [render-file]]))

(defonce calc-state (atom [0]))

(defn current-display []
  (let [currently (first @calc-state)]
    (str (html [:div#display
                [:div.input-so-far.num-display "~"]
                [:div.currently.num-display  currently]]))))

(defn base-html []
  [:html {:lang "en"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:link {:rel "stylesheet" :href "/calc.css" :type "text/css" :title "default"}]
    [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]
    [:title "bb-htmx-dentaku"]]
   [:body
    [:div.background
     [:div#calc
      [:h1.title "bb-htmx-dentaku"]
      [:div (current-display)
       [:div#buttons
        {:hx-post "num-input"
         :hx-trigger "click from:.calc-input"
         :hx-target "#display"}
        [:a.calc-input.one-button {:value 1} "1"]
        [:a.calc-input.one-button {:value 2} "2"]
        [:a.calc-input.one-button {:value 3} "3"]
        [:a.calc-input.one-button {:value \D} "÷"]
        [:a.calc-input.one-button {:value 4} "4"]
        [:a.calc-input.one-button {:value 5} "5"]
        [:a.calc-input.one-button {:value 6} "6"]
        [:a.calc-input.one-button {:value \M} "×"]
        [:a.calc-input.one-button {:value 7} "7"]
        [:a.calc-input.one-button {:value 8} "8"]
        [:a.calc-input.one-button {:value 9} "9"]
        [:a.calc-input.one-button {:value \-} "-"]
        [:a.calc-input.one-button {:value \C} "c"]
        [:a.calc-input.one-button {:value 0} "0"]
        [:a.calc-input.one-button {:value \=} "="]
        [:a.calc-input.one-button {:value \+} "+"]]]]]
    [:script (render-file "calc.js" {})]]])

(defn num-input [req]
  (let [{:strs [num]} (payload->map req)]
    (swap! calc-state update-in [0] (fn [v] (+ (* v 10) (Integer/parseInt num))))
    (current-display)))

(defn router [req]
  (let [paths (some-> (:uri req) (str/split #"/") rest vec)
        verb (:request-method req)]
    (match [verb paths]
      [:get []] {:headers {"Content-Type" "text/html; charset=utf-8"}
                 :body (let [base (base-html)]
                         (str (html {:escape-strings? false} base)))}
      [:post ["num-input"]] {:body (num-input req)}
      [:get ["calc.css"]] {:headers {"Content-Type" "text/css"}
                           :body (slurp (io/resource "calc.css"))}
      :else {:status 404 :body "not found"})))
