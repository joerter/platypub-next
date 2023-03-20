(ns platypub
  (:require [rum.core :as rum]
            [clj-http.client :as http]
            [clojure.edn :as edn]
            [markdown-to-hiccup.core :as m]))

(comment
  (get-secret :strapi/api-token)
  (def article (nth (get-articles) 0))
  (article->html article)
  (get-config :strapi/base-url))

(defn get-edn [file k]
  (k (edn/read-string (slurp file))))

(def get-config (partial get-edn "config.edn"))

(def get-secret (partial get-edn "secrets.edn"))

(defn markdown->hiccup [markdown]
  (->> markdown
       (m/md->hiccup)
       (m/component)))

(defn base-html [content]
  [:html
   {:lang "en-US"}
   [:head
    [:title "Test Blog"]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    [:meta {:charset "utf-8"}]]
   [:body content]])

(defn get-articles []
  (->
   (http/get (str (get-config :strapi/base-url) (get-config :strapi/content-type)) {:as :json :headers {:Authorization (str "Bearer " (get-secret :strapi/api-token))}})
   :body :data))

(defn article->html [{:keys [attributes]}]
  (let [{:keys [Title publishedAt Content]} attributes] [:article
                                                         [:h3 Title]
                                                         [:h5 publishedAt]
                                                         [:p (markdown->hiccup Content)]]))

(defn render! [page hiccup]
  (spit page (str "<!DOCTYPE html>\n" (rum/render-static-markup hiccup))))

(defn index-page []
  (render! "index.html"
           (base-html
            [:main
             [:h1 "Test Blog"]
             [:h2 "Recent Posts"]
             (map article->html (get-articles))])))

(defn run [opts]
  (index-page))
