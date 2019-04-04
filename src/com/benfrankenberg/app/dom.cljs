(ns com.benfrankenberg.app.dom
  (:require
   [goog.object :as obj]
   [clojure.string :refer [join split]]
   [com.benfrankenberg.app.util :refer [query query-all]]))

(defn el->classes
  "
  Takes a DOM element.
  Returns a set of class names.
  "
  [el]
  (-> (.-className el)
      (split #" ")
      (set)))

(defn toggle-class!
  "
  Takes a DOM element and variadic class names.
  Class-names not present on the element will be added.
  Class-names present on the element will be removed.
  Mutates the DOM element.
  "
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
  "
  Removes any of the target-classes from the DOM element.
  Takes a DOM element and variadic class names.
  Returns the DOM element.
  Mutates the DOM element.
  "
  [el target-classes]
  (set! (.-className el)
        (->> el
             (el->classes)
             (remove (set target-classes))
             (join " ")))
  el)

(defn add-classes!
  "
  Adds all target-classes to the DOM element.
  Takes a DOM element and variadic class names.
  Returns DOM element.
  Mutates the DOM element.
  "
  [el target-classes]
  (set! (.-className el)
        (->> el
             (el->classes)
             (into (set target-classes))
             (join " ")))
  el)

(defn swap-class!
  "
  Removes list of class names from child elements in the container element
  then adds the class names to the target element.
  Takes a container element, target element, and variadic list of class names.
  Returns the DOM element.
  Mutates the DOM element.
  "
  [container el & class-names]
  (let [target-classes (str "." (join "." class-names))]
    (doseq [el (query-all container target-classes)]
      (remove-classes! el class-names))
    (add-classes! el class-names)
    el))


(defn style!
  "
  Change several style properties of an element.
  Takes a target DOM element and a list of key-value pairs.
  Mutates the style properties.
  Returns the target DOM element.

  Example:
  (style! (.querySelector js/document \"body\") :opacity 1 :height \"300px\")
  "
  [el & kvs]
  (let [el-style (-> el (.-style))]
    (loop [kvs kvs]
      (let [[property value & remaining] kvs]
        (when (some? kvs)
          (obj/set el-style (name property) value)
          (recur remaining))))
    el))
