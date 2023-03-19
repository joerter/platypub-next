(ns platypub
  (:require [rum.core :as rum]
            [clj-http.client :as http]
            [clojure.edn :as edn]
            [markdown-to-hiccup.core :as m]))

(comment
  (get-secret :strapi/api-token)
  (def post (nth (get-posts) 0))
  (post->html post)
  (base-html [:h1 "hi"]))

(defn get-secret [k]
  (k (edn/read-string (slurp "secrets.edn"))))

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

(defn get-posts []
  (->
   (http/get "http://localhost:1337/api/posts" {:as :json :headers {:Authorization (str "Bearer " (get-secret :strapi/api-token))}})
   :body :data))

(defn post->html [{:keys [attributes]}]
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
             (map post->html (get-posts))])))

(defn run [opts]
  (index-page))
