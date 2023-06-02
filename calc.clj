(ns calc
  (:require [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [common :refer [payload->map]]
            [hiccup2.core :refer [html]]
            [selmer.parser :refer [render-file]]))

(defonce calc-state (atom [0]))

(defonce op-symbol
  {\D "÷" \M "×" \- "-" \+ "+"})

(defn show-num [v]
  (cond
    (ratio? v) (format "%.3f" (float v))
    (float? v) (format "%.3f" v)
    :else v))

(defn current-display []
  (let [[v1 op v2] @calc-state]
    (str (html [:div#display
                [:div.input-so-far.num-display (cond (= op \=) "="
                                                     (some? op) (str (show-num v2) " " (op-symbol op))
                                                     :else "~")]
                [:div.currently.num-display (show-num v1)]]))))

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

(defn math-op [v1 op v2]
  (case op
    \D (/ v2 v1)
    \M (* v2 v1)
    \- (- v2 v1)
    \+ (+ v2 v1)))

(defn num-input [req]
  (let [{:strs [num]} (payload->map req)]
    (let [num-char (first num)]
      (cond
        (Character/isDigit num-char)
        (swap! calc-state
               (fn [[v1 op v2]]
                 (println "NUM" v1 op)
                 (if (= op \=)
                   [(Integer/parseInt num)]
                   [(+ (* v1 10) (Integer/parseInt num)) op v2])))

        (#{\D \M \- \+} num-char)
        (swap! calc-state
               (fn [[v1 op v2]]
                 (println "OP" v1 op)
                 (if op
                   [0 num-char (math-op v1 op v2)]
                   [0 num-char v1])))

        :else (case num-char
                \= (swap! calc-state
                          (fn [[v1 op v2]]
                            (if op
                              [(math-op v1 op v2) \=]
                              [v1 \=])))
                \C (reset! calc-state [0]))))
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
