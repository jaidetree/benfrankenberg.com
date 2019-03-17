(ns src.com.benfrankenberg.site.home)

(defn background
  []
  [:div.background nil])

(defn hero
  []
  [:header.hero
    [:div.hero__inner
      [:h1.hero__title.display "Benjamin Frankenberg"]
      [:h2.hero__subtitle.body "Actor &bull; Artist &bull; Swordsman"]]])

(defn bio
  []
  [:div.group.bio
    [:h3.display "About Benjamin Frankenberg"]
    [:p.text.mt "Ben is a professional actor with experience in a wide selection of mediums such as theater, film, TV, and movies. He breathes life into characters by incorporating both technical acting techniques and his personal artistic vision to delight and inspire audiences. Like many great actors, Ben works hard to provide strong characterization into every element in both grand movements and subtle ticks to create memorable characters that reach deep within our minds and souls. Ben currently holds a Bachelors in Performing Arts degree from Savannah College School of Art and Design."]])

(defn links
  []
  [:div.links
    [:a.btn.links__link
      {:href "/downloads/benjamin-frankenberg-resume.pdf"}
      [:i.inline.fa.fa-file-pdf]
      "Download Resume"]
    [:a.btn.links__link
      {:href "https://www.imdb.com/name/nm5528224/"}
      [:i.inline.fab.fa-imdb]
      "View Credits"]
    [:a.btn.links__link
      {:href "mailto:benfrankenberg@gmail.com"}
      [:i.inline.far.fa-envelope]
      "Email Ben"]])

(defn about
  []
  [:section.section.about
    [:div.column.about__content
      (bio)
      (links)]
    [:div.column.about__headshot
      [:div.profile-photo
        [:img.profile-photo__img {:src "/img/ben_hero.jpg"}]]]])

(defn render
  []
  (list
    ["!DOCTYPE", "html"]
    [:html
      [:head
        [:title "Benjamin Frankenberg: Actor, Artist, Swordsman"]
        [:meta {:charset "utf-8"}]
        [:meta {:http-equiv "X-UA-Compatible"
                :content "IE=edge,chrome=1"}]
        [:meta {:name "viewport"
                :content "width=device-width, initial-scale=1.0, user-scalable=no"}]
        [:link {:rel "stylesheet"
                :href "https://use.fontawesome.com/releases/v5.7.2/css/all.css"
                :crossorigin "anonymous"}]
        [:link {:rel "stylesheet"
                :href "/css/style.css"}]
        [:link {:rel "stylesheet"
                :href "https://fonts.googleapis.com/css?family=Alegreya|Raleway"}]
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
        [:div.page
          (list (background)
                (hero)
                (about))]]]))

