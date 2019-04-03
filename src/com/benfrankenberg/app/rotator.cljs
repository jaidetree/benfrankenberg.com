(ns com.benfrankenberg.app.rotator
  (:require
   [bacon :as bacon]
   [com.benfrankenberg.app.animation :as animation]
   [com.benfrankenberg.app.dom :as dom]
   [com.benfrankenberg.app.state :refer [action? bus create-store gen-action]]
   [com.benfrankenberg.app.stream :as stream]
   [com.benfrankenberg.app.swipe :refer [swipe]]
   [com.benfrankenberg.app.util :refer [prop query query-all]]
   [com.benfrankenberg.app.viewport :as viewport]))

;; Reducers
;; ---------------------------------------------------------------------------

;; Map action types to reducer functions to update the db state
(def reducers {:rotate  (fn [db {:keys [data]}]
                          (merge db data))
               :rotated (fn [db {:keys [data]}]
                          (merge db {:current data
                                     :from nil
                                     :to nil}))
               :start   (fn [db {:keys [data]}]
                          (merge db data))})

;; Utils
;; ---------------------------------------------------------------------------

(defn query-initial-index
  "
  Find index of an element with the active class within a list of elements.
  Takes a list of slide elements
  Returns a number or nil.
  "
  [slides]
  (some->> slides
           (map-indexed vector)
           (filter (fn [[_ el]] (.includes (.-className el) "active")))
           (first)
           (first)))

(defn query-container
  "
  Takes an action map {:action :type :data {:selector \".next\"}}
  Returns DOM element matching :data :selector string.
  "
  [action]
  (query (get-in action [:data :selector])))

(defn animate-btn-click
  "
  Takes a button DOM element to animate.
  Triggers an animation by adding the active class then removing it after
  about 500ms.
  Returns a bacon stream subscription.
  "
  [el]
  (-> (stream/of el)
      (animation/delay-frame)
      (.doAction #(dom/add-classes! % ["active"]))
      (.delay 500)
      (animation/delay-frame)
      (.onValue #(dom/remove-classes! % ["active"]))))

(defn scale-btn
  "
  Scales the prev or next rotator buttons based on swipe amount.
  Gives the user a hint for when the rotator will respond to the swipe.

  Takes a swipe hash-map:
  el       DOMElement - Container DOM element of rotator
  scale    float      - Scale of swipe to 100px target
  selector str        - Either \".next\" or \".prev\"

  Returns nil.
  "
  [{:keys [el scale selector]}]
  (let [btn (query el selector)
        style (.-style btn)
        size (+ 1 (* scale 0.5))
        opacity (+ 0.5 (* scale 0.5))]
    (set! (.-transform style)
          (str "scale(" size ")"))
    (set! (.-opacity style)
          opacity)))

(defn reset-btn-scale
  "
  Resets the scale of the rotator buttons after swipe or button click.

  Takes a swipe hash-map:
  el DOMElement - Container DOM element of rotator
  "
  [{:keys [el]}]
  (doseq [selector [".prev" ".next"]]
    (let [btn (query el selector)
          style (.-style btn)]
      (set! (.-transform style)
            "scale(1)")
      (set! (.-opacity style)
            ""))))

(defn next-idx
  "
  Get the next index.
  Takes a target index number starting from 1 and total.
  Returns the next slide, restarts at 1 if target index is greater than total.
  "
  [idx total]
  (if (> idx total) 1 idx))

(defn prev-idx
  "
  Get the previous index.
  Takes a target index number starting from 1 and the total.
  Returns the prev slide. Restarts at total if target index is less than 0.
  "
  [idx total]
  (if (<= idx 0) total idx))

(defn prepare-transition
  "
  Prepares the target elements for a rotation transition.

  Takes a rotate map:
  from-el       DOMElement - Current target element
  to-el         DOMElement - Next target element
  direction-cls str        - Class name like \"forwards\" or \"backwards\"

  Mutates the class names of the target elements.
  Returns the next target el.
  "
  [{:keys [from-el to-el direction-cls]}]
  (dom/toggle-class! from-el "transition" "from" direction-cls)
  (dom/toggle-class! to-el   "transition" "to"   direction-cls))

(defn start-transition
  "
  Starts rotating the target elements by adding a rotate class.

  Takes a rotate map:
  from-el       DOMElement - Current target element
  to-el         DOMElement - Next target element

  Mutates the class names of the target elements.
  Returns the next target el.
  "
  [{:keys [from-el to-el]}]
  (dom/toggle-class! from-el "rotate")
  (dom/toggle-class! to-el   "rotate"))

(defn end-transition
  "
  Removes transition classes after a rotation.

  Takes a rotate map:
  from-el       DOMElement - Current target element
  to-el         DOMElement - Next target element
  direction-cls str        - Class name like \"forwards\" or \"backwards\"

  Mutates the class names of the target elements.
  Returns the next target el.
  "
  [{:keys [from-el to-el direction-cls]}]
  (dom/toggle-class! from-el "transition" "from" "rotate" direction-cls)
  (dom/toggle-class! to-el   "transition" "to"   "rotate" direction-cls))

(defn translate-x
  [el percent]
  (set! (-> el (.-style) (.-transform))
        (str "translateX(" percent "%)")))

(defn animate-slides
  [{:keys [from-el to-el direction]}]
  (println "animation start")
  (-> (animation/duration 800)
      (.map animation/sine)
      (.doAction
       (fn [progress]
         (let [from-progress (* progress -100)
               to-progress (- 100 (* progress 100))]
            (translate-x from-el from-progress)
            (translate-x to-el to-progress))))
      (.last)))

(defn rotate-slide-elements
  "
  Rotate two slide elements by toggling classes over time. Kinda like a
  a finite state machine.

  Takes a rotate map:
  direction keyword - Class name like :forwards or :backwards
  from      int     - Current target index starting from 1
  to        int     - Next target index starting from 1
  selector  str     - Selector of rotator container.

  Mutates the class names of the target elements.
  Returns a bacon stream of the input rotate map.
  "
  [{:keys [direction from selector to] :as state}]
  (let [container (query selector)
        from-el (query container (str ".slide[data-id=\"" from "\"]"))
        to-el   (query container (str ".slide[data-id=\"" to "\"]"))]
    (-> (stream/of state)
        (.map #(merge % {:from-el from-el
                         :to-el to-el
                         :direction-cls (name direction)}))
        ; (animation/delay-frame)
        (.flatMap animate-slides)
        ; (.doAction prepare-transition)
        ; (animation/delay-frame)
        ; (.doAction start-transition)
        ; (.delay 800)
        ; (.doAction end-transition)
        (.doAction #(println "animation end"))
        (.doAction #(dom/swap-class! (query container ".slides") to-el "active"))
        (.map (constantly to)))))

(defn set-container-height
  "
  Takes a rotator container DOM element.
  Mutates the height of the container to match the height of its first slide.
  "
  [container]
  (let [el (query container ".slide img")]
    (-> (.fromEvent bacon el "load")
        (.startWith true)
        (.filter #(viewport/mobile?))
        (.onValue (fn [_]
                    (set! (-> container (.-style) (.-height))
                          (str (.-clientHeight el) "px")))))))

(defn update-progress-bar
  "
  Updates the progress bar on each rotation.

  Takes a rotate map:
  current  int - Index of current slide starting at 1
  selector str - Selector of rotator container.
  total    int - Total number of slides.

  Mutates the width of the progress bar element.
  "
  [{:keys [selector current total] :as state}]
  (let [container (query selector)
        el (query container ".headshots__progress")
        progress (.toFixed (* (/ (dec current) total) 100) 4)]
    (set! (-> el (.-style) (.-left))
          (str progress "%"))))

;; Effects
;; ---------------------------------------------------------------------------

(defn button-events
  "
  Handles click events from the .next and .prev buttons.
  Takes actions bacon stream and a state bacon stream.

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Initial data

  Returns bacon stream of action hash-maps

  Examples:

  Input actions:
  {:type :start :data {:current 1 :selector \".headshots\" :total 3}}

  Output actions:
  {:type :next :data \".next\"}
  {:type :prev :data \".prev\"}
  "
  [actions state]
  (-> actions
      (action? :start)
      (.map query-container)
      (.flatMap
       (fn [container]
         (-> [(query container ".prev")
              (query container ".next")]
             (stream/from)
             (.flatMap #(.fromEvent bacon % "click"))
             (.map #(-> % (.-currentTarget) (.-value)))
             (.map #(gen-action (keyword %) (str "." %))))))))

(defn button-ui-fx
  "
  Triggers an animation on the corresponding button when a rotation occurs.
  Adds class .active to target button and then removes it 500ms later

  Takes a bacon stream of action hash-maps and a state stream.

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data str      DATA - Class name of activated button

  Triggers the animation class on the target button.
  Returns an empty stream of actions.

  Examples:

  Input actions:
  {:type :next :data \".next\"}
  {:type :prev :data \".prev\"}
  "
  [actions state]
  (-> actions
      (action? :prev :next)
      (.map (prop :data))
      (.withLatestFrom
       (-> state
           (.map (prop :selector))
           (.map query))
       (fn [selector container]
         (query container selector)))
      (.doAction animate-btn-click)
      (.filter false)))

(defn navigate
  "
  Fx-handler for next actions. Calculates the next slide index and queues
  a rotation.

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Action data used to update the progres bar with

  Takes a bacon stream of action hash-maps and a state stream.
  Returns a bacon stream of rotate actions for the next slide.

  Examples:

  Input actions:
  {:type :prev :data \".prev\"}
  {:type :next :data \".next\"}

  Output actions:
  {:type :rotate :data {:current 3
                        :selector \".headshots\"
                        :total 3
                        :from 1
                        :to 3
                        :direction \"backwards\"}}
  {:type :rotate :data {:current 2
                        :selector \".headshots\"
                        :total 3
                        :from 1
                        :to 2
                        :direction \"forwards\"}}
  "
  [actions state]
  (-> actions
      (action? :prev :next)
      (.withLatestFrom state (fn [{:keys [type]} state]
                               (assoc state :type type)))
      (.map
       (fn [{:keys [type total current] :as state}]
         (let [idx (if (= type :next)
                     (next-idx (inc current) total)
                     (prev-idx (dec current) total))
               data {:current idx
                     :from current
                     :to idx
                     :direction (if (= type :next)
                                  "forwards"
                                  "backwards")}]
           {:type :rotate
            :data (merge state data)})))))

(defn progress-bar-fx
  "
  Fx-handler for the progress bar at the bottom of the rotator.
  Takes a bacon stream of action hash-maps and a state stream

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Action data used to update the progres bar with

  Mutates the progress-bar component.
  Returns an empty bacon stream.

  Examples:

  Input actions:
  {:type :start :data {:current 1 :selector \".headshots\" :total 3}}
  {:type :rotate :data {:current 2
                        :selector \".headshots\"
                        :total 3
                        :from 1
                        :to 2
                        :direction \"forwards\"}}
  "
  [actions state]
  (-> actions
      (action? :start :rotate)
      (.map #(get % :data))
      (.doAction update-progress-bar)
      (.filter false)))

(defn rotate
  "
  Fx-handler for rotating between two slides.
  Takes a bacon stream of action hash-maps and a state stream

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Action data used to update the progres bar with

  Mutates the from and to slides by index.
  Returns a bacon stream of actions.

  Examples:

  Input actions:
  {:type :rotate :data {:current 2
                        :selector \".headshots\"
                        :total 3
                        :from 1
                        :to 3
                        :direction \"backwards\"}}
  {:type :rotate :data {:current 2
                        :selector \".headshots\"
                        :total 3
                        :from 1
                        :to 2
                        :direction \"forwards\"}}

  Output actions:
  {:type :rotated :data {:current 2
                         :selector \".headshots\"
                         :total 3
                         :from 1
                         :to 3
                         :direction \"backwards\"}}
  {:type :rotated :data {:current 2
                         :selector \".headshots\"
                         :total 3
                         :from 1
                         :to 2
                         :direction \"forwards\"}}
  "
  [actions state]
  (-> actions
      (action? :rotate)
      (.map #(get % :data))
      (.flatMapConcat rotate-slide-elements)
      (.map (gen-action :rotated))))

(defn swipe-events
  "
  Fx-handler for responding to swipe gestures on mobile devices.
  Takes a bacon stream of action hash-maps and a state stream

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Action data used to update the progres bar with

  Mutates the nav buttons style properties from direction of swipe.

  Returns a bacon stream of actions.

  Examples:

  Input actions:
  {:type :start :data {:current 2 :selector \".headshots\" :total 3}}

  Output actions:
  {:type :swipe :data {:direction :next :selector \".next\"}}
  {:type :swipe :data {:direction :prev :selector \".prev\"}}
  "
  [actions state]
  (-> actions
      (action? :start)
      (.map query-container)
      (.flatMap #(swipe {:el % :on-move scale-btn :on-end reset-btn-scale}))
      (.map #(select-keys % [:direction :selector]))
      (.map (gen-action :swipe))))

(defn swipe-hint
  "
  Fx-handler for displaying a swipe hint when a user taps the nav buttons on
  mobile devices.
  Takes a bacon stream of action hash-maps and a state stream

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Action data used to update the progres bar with

  Returns an empty bacon stream.

  Examples:

  Input actions:
  {:type :rotate :data {:current 2
                        :selector \".headshots\"
                        :total 3
                        :from 3
                        :to 1
                        :direction \"backwards\"}}
  {:type :rotate :data {:current 2
                        :selector \".headshots\"
                        :total 3
                        :from 1
                        :to 2
                        :direction \"forwards\"}}
  "
  [actions state]
  (-> actions
    (.takeWhile #(or (viewport/mobile?)))
    (action? :rotate)
    (.flatMap (constantly state))
    (.take 1)
    (.map #(query (:selector %)))
    (.takeUntil (-> actions
                    (action? :swipe)))
    (.doAction (fn [container]
                 (let [el (query container ".swipe-hint")]
                   (dom/add-classes! el ["active"]))))
    (.flatMap #(-> actions
                   (action? :rotate)
                   (.take 1)
                   (.map %)))
    (.doAction (fn [container]
                 (let [el (query container ".swipe-hint")]
                   (dom/remove-classes! el ["active"]))))
    (.filter false)))

(defn swipe-rotate
  "
  Fx-handler for responding to swipe gestures with a rotation.
  Takes a bacon stream of action hash-maps and a state stream

  Actions hash-map:
  type keyword  TYPE - Type of action dispatched like :start or :rotate
  data hash-map DATA - Action data used to update the progres bar with

  Returns an empty bacon stream.

  Examples:

  Input actions:
  {:type :swipe :data {:direction :prev :selector \".prev\"}}
  {:type :swipe :data {:direction :next :selector \".next\"}}

  Output actions:
  {:type :next :data \".next\"}
  {:type :prev :data \".prev\"}
  "
  [actions state]
  (-> actions
      (action? :swipe)
      (.map #(:data %))
      (.map #(gen-action (:direction %) (:selector %)))))

(def fx
  [button-events
   button-ui-fx
   navigate
   progress-bar-fx
   rotate
   swipe-events
   swipe-hint
   swipe-rotate])

;; Public API
;; ---------------------------------------------------------------------------

(defn rotator
  "
  Public API to rotate between several .slide elements, swipe gestures,
  nav button clicks, animations, and the progress bar.

  Takes a selector string for the rotator container including slides and ui.
  Returns a store map with access to the bacon streams containing the actions
  dispatched through it and a bacon stream with the latest state.
  "
  [selector]
  (let [container (query selector)
        slides (query-all container ".slide")
        index (or (query-initial-index slides) 1)
        store (create-store {:current index} reducers fx)
        {:keys [dispatch state]} store]
    (set-container-height container)
    (dispatch {:type :start
               :data {:total (count slides)
                      :selector selector
                      :current 1}})
    store))
