(ns calc
  (:require [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [common :refer [payload->map]]
            [hiccup2.core :refer [html]]
            [selmer.parser :refer [render-file]]))

(def base-html
  (list [:html {:lang "en"}
         [:head
          [:meta {:charset "UTF-8"}]
          [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          [:link {:rel "stylesheet" :href "/calc.css" :type "text/css" :title "default"}]
          [:script {:src "https://unpkg.com/htmx.org@1.9.2"}]
          [:title "bb-calc"]]
         [:body
          [:div.background
           [:div#calc
            [:h1.title "bb-calc"]
            [:div
             [:div.input-so-far.num-display "1234 +"]
             [:div.currently.num-display "1000"]
             [:div#buttons
              {:hx-post "num-input"
               :hx-trigger "click from:.calc-input"
               :hx-swap "none"}
              [:a.calc-input.one-button {:value 1} "1"]
              [:a.calc-input.one-button {:value 2} "2"]
              [:a.calc-input.one-button {:value 3} "3"]
              [:a.calc-input.one-button {:value \D} "D"]
              [:a.calc-input.one-button {:value 4} "4"]
              [:a.calc-input.one-button {:value 5} "5"]
              [:a.calc-input.one-button {:value 6} "6"]
              [:a.calc-input.one-button {:value \M} "M"]
              [:a.calc-input.one-button {:value 7} "7"]
              [:a.calc-input.one-button {:value 8} "8"]
              [:a.calc-input.one-button {:value 9} "9"]
              [:a.calc-input.one-button {:value \-} "-"]
              [:p.one-button]
              [:a.calc-input.one-button {:value 0} "0"]
              [:a.calc-input.one-button {:value \=} "="]
              [:a.calc-input.one-button {:value \+} "+"]]]]]
          [:script (render-file "calc.js" {})]]]))

(defn num-input [req]
  (let [{:strs [num]} (payload->map req)]
    (println "this is" num)))

(defn router [req]
  (let [paths (some-> (:uri req) (str/split #"/") rest vec)
        verb (:request-method req)]
    (match [verb paths]
      [:get []] {:headers {"Content-Type" "text/html; charset=utf-8"}
                 :body (str (html {:escape-strings? false} base-html))}
      [:post ["num-input"]] (do (num-input req)
                                {:status 200})
      [:get ["calc.css"]] {:headers {"Content-Type" "text/css"}
                           :body (slurp (io/resource "calc.css"))}
      :else {:status 404 :body "not found"})))
