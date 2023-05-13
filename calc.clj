(ns calc
  (:require [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [selmer.parser :refer [render-file]]))

(defn router [req]
  (let [paths (some-> (:uri req) (str/split #"/") rest vec)
        verb (:request-method req)]
    (println paths)
    (match [verb paths]
      [:get []] {:body (render-file "calc.html" {})}
      [:get ["calc.css"]] {:headers {"Content-Type" "text/css"}
                           :body (slurp (io/resource "calc.css"))}
      :else {:status 404 :body "not found"})))
