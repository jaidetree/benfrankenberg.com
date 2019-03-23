(ns com.benfrankenberg.app.dom
  (:require
   [clojure.string :refer [join split]]
   [com.benfrankenberg.app.util :refer [query query-all]]))

(defn el->classes
  [el]
  (-> (.-className el)
      (split #" ")
      (set)))

(defn toggle-class!
  [el & class-names]
  (let [classes (reduce (fn [classes class-name]
                          (if (contains? classes class-name)
                            (disj classes class-name)
                            (conj classes class-name)))
                        (el->classes el)
                        class-names)]
    (set! (.-className el) (join " " classes))
    el))

(defn remove-classes!
  [el target-classes]
  (set! (.-className el)
        (->> el
             (el->classes)
             (remove (set target-classes))
             (join " "))))

(defn add-classes!
  [el target-classes]
  (set! (.-className el)
        (->> el
             (el->classes)
             (into (set target-classes))
             (join " "))))

(defn swap-class!
  [container el & class-names]
  (let [target-classes (str "." (join "." class-names))]
    (doseq [el (query-all container target-classes)]
      (remove-classes! el class-names))
    (add-classes! el class-names)
    el))
