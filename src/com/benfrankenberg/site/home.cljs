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
    [:a.btn.links__link.resume
      {:href "/downloads/benjamin-frankenberg-resume.pdf"}
      [:i.inline.fa.fa-file-pdf]
      "Download Resume"]
    [:a.btn.links__link.credits
      {:href "https://www.imdb.com/name/nm5528224/"}
      [:i.inline.fab.fa-imdb]
      "View Credits"]
    [:a.btn.links__link.email
      {:href "mailto:benfrankenberg@gmail.com"}
      [:i.inline.far.fa-envelope]
      "Email Ben"]])

(defn headshot
  [_ idx]
  (let [src (str "/img/ben-frankenberg-headshot-" idx ".jpg")]
    [:div.headshot
      {:style {:background-image (str "url(" src ")")}}
      [:img.headshot__img {:alt (str "Headshot #" idx " of Ben Frankenberg")
                           :src src}]
      [:a.btn.headshot__download
        {:href src
         :download true}
        [:i.inline.fas.fa-download]
        "Download"]]))

(defn headshots
  [_ idxs]
  [:div.headshots
    [:ul.headshots__list.slides
     (for [idx idxs]
       [:li.headshots__item.slide
        {:data-id idx
         :class (when (= idx 1) "active")}
        [headshot idx]])]
    [:div.headshots__ui
      [:button.headshots__btn.prev
        {:value "prev"}
        [:i.fas.fa-chevron-left]]
      [:button.headshots__btn.next
        {:value "next"}
        [:i.fas.fa-chevron-right]]]])

(defn about
  []
  [:section.section.about
    [:div.column.about__content
      (bio)
      (links)]
    [:div.column.about__headshot
      [headshots [1 2 3 4]]]])

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
        [:meta {:name "msapplication-TileColor"
                :content "#da532c"}]
        [:meta {:name "theme-color"
                :content "#ffffff"}]
        [:meta {:property "og:image:width"
                :content "279"}]
        [:meta {:property "og:image:height"
                :content "279"}]
        [:meta {:property "og:description"
                :content "Actor &bull; Artist &bull; Swordsman"}]
        [:meta {:property "og:title"
                :content "Ben Frankenberg"}]
        [:meta {:property "og:url"
                :content "https://benfrankenberg.com"}]
        [:meta {:property "og:image"
                :content "https://benfrankenberg.com/og-image.jpg"}]
        [:link {:rel "apple-touch-icon"
                :sizes "180x180"
                :href "/apple-touch-icon.png"}]
        [:link {:rel "icon"
                :type "image/png"
                :sizes "32x32"
                :href "/favicon-32x32.png"}]
        [:link {:rel "icon"
                :type "image/png"
                :sizes "16x16"
                :href "/favicon-16x16.png"}]
        [:link {:rel "manifest"
                :href "/site.webmanifest"}]
        [:link {:rel "mask-icon"
                :href "/safari-pinned-tab.svg"}]
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
        [:script
         {:type "text/javascript"}
         "window.document.body.style.opacity = 0;"]
        [:div.page
          (list (background)
                (hero)
                (about))]
        [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/bacon.js/2.0.11/Bacon.min.js"}]
        [:script {:src "/js/app.js"}]]]))
