(ns src.com.benfrankenberg.site.home)

(defn background
  []
  [:div.background nil])

(defn hero
  []
  [:header.hero
    [:div.hero__inner
      [:h1.hero__title.display "Benjamin Frankenberg"]
      [:h2.hero__subtitle.body "Actor &bull; Artist &bull; Swordsman"]]
    [:div.scroll-hint
     [:span.scroll-hint__text "Scroll down"]
     [:i.scroll-hint__icon.fas.fa-chevron-down]
     [:span.scroll-hint__text "Discover more"]]])

(defn bio
  []
  [:div.group.bio
    [:h3.display "About Benjamin Frankenberg"]
    [:p.text.mt "Ben is a professional actor working in a wide range of mediums such as film, television, theater and experimental works.
                 They breathe life into their characters by incorporating both studied performance techniques and their own artistic vision to captivate and inspire audiences.
                 Through the lens of their experience, Ben&rsquo;s work provides intuitive, enigmatic and dynamic characters whose spirits linger long in the minds and memory of those who witness.
                 Ben holds a Bachelor in Performing Arts degree from the Savannah College of Art & Design and has trained in stage combat through the Society of American Fight Directors."]])

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
  (let [file (str "ben-frankenberg-headshot-" idx ".jpg")
        img (str "/img/" file)
        download (str "/downloads/" file)]
    [:div.headshot
      {:style {:background-image (str "url(" img ")")}}
      [:img.headshot__img {:alt (str "Headshot #" idx " of Ben Frankenberg")
                           :src img}]
      [:a.btn.headshot__download
        {:href download
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
    [:div.headshots__progress-bar
     [:div.headshots__progress]]
    [:div.headshots__ui
      [:button.headshots__btn.prev
        {:value "prev"}
        [:i.fas.fa-chevron-left]]
      [:button.headshots__btn.next
        {:value "next"}
        [:i.fas.fa-chevron-right]]
      [:div.swipe-hint
       [:i.swipe-hint__icon.fas.fa-hand-point-left]
       [:span.swipe-hint__text "Swipe left or right to navigate photos"]
       [:i.swipe-hint__icon.fas.fa-hand-point-right]]]])


(defn about
  []
  [:section.section.about
    [:div.column.about__content
      (bio)
      (links)]
    [:div.column.about__headshot
      [headshots [1 2 3 4]]]])

(defn head-mobile-meta
  []
  (list
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0, user-scalable=no"}]))

(defn head-open-graph-meta
  []
  (list
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
            :content "https://benfrankenberg.com/og-image.jpg"}]))


(defn head-favicon
  []
  (list
    [:meta {:name "msapplication-TileColor"
            :content "#da532c"}]
    [:meta {:name "theme-color"
            :content "#ffffff"}]
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
            :href "/safari-pinned-tab.svg"}]))

(defn head-fonts
  []
  (list
    [:link {:rel "stylesheet"
            :href "https://fonts.googleapis.com/css?family=Alegreya|Raleway"}]
    [:link {:rel "stylesheet"
            :href "https://use.fontawesome.com/releases/v5.7.2/css/all.css"
            :crossorigin "anonymous"}]))

(defn styles
  []
  (list
        [:link {:rel "stylesheet"
                :href "/css/style.css"}]
        [:link {:rel "stylesheet"
                 :media "screen and (max-width: 929px)"
                 :href "/css/mobile.css"}]
        [:link {:rel "stylesheet"
                :media "screen and (min-width: 930px)"
                :href "/css/desktop.css"}]))

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
        (head-mobile-meta)
        (head-open-graph-meta)
        (head-favicon)
        (head-fonts)
        (styles)]
      [:body
        [:script
         {:type "text/javascript"}
         "window.document.body.style.opacity = 0;"]
        [:div.page
          (list (background)
                (hero)
                (about))]
        [:script {:src "https://cdnjs.cloudflare.com/ajax/libs/bacon.js/3.0.0/Bacon.min.js"}]
        [:script {:src "/js/app.js"}]]]))
