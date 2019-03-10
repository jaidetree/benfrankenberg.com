(ns src.com.benfrankenberg.site.home)

(defn render
  []
  '(["!DOCTYPE", "html"]
    [:html
      [:head
        [:title "Benjamin Frankenberg: Stuntman, Swordsman, Actor"]
        [:link {:rel "stylesheet"
                :href "/css/style.css"}]
        [:link {:rel "stylesheet"
                :href "https://fonts.googleapis.com/css?family=IM+Fell+English"}]]
      [:body
        [:header.hero
          [:hero__inner
            [:h1 "Benjamin Frankenberg"]]]]]))

