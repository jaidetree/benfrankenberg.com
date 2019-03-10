(ns src.com.benfrankenberg.site.home)

(defn render
  []
  '(["!DOCTYPE", "html"]
    [:html
      [:head
        [:title "Benjamin Frankenberg: Actor, Artist, Swordsman"]
        [:meta {:charset "utf-8"}]
        [:meta {:http-equiv "X-UA-Compatible"
                :content "IE=edge,chrome=1"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1.0, user-scalable=no"}]
        [:link {:rel "stylesheet"
                :href "/css/style.css"}]
        [:link {:rel "stylesheet"
                :href "https://fonts.googleapis.com/css?family=IM+Fell+English|Slabo+27px"}]
        [:link {:rel "stylesheet"
                 :media "screen and (max-width: 929px)"
                 :href "/css/mobile.css"}]
        [:link {:rel "stylesheet"
                :media "screen and (min-width: 768px) and (max-width: 929px)"
                :href "/css/tablet.css"}]
        [:link {:rel "stylesheet"
                :media "screen and (min-width: 930px)"
                :href "/css/desktop.css"}]]
      [:body
        [:header.hero
          [:div.hero__inner
            [:h1.display "Benjamin Frankenberg"]
            [:h2.body "Actor &bull; Artist &bull; Swordsman"]]]]]))

