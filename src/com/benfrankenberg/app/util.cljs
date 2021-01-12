(ns com.benfrankenberg.app.util
  (:require [bacon :as bacon]))

(defn prop
  "
  Takes a key, usually a keyword.
  Returns a function that takes a hash map.
  Calling the returned with a hash-map returns the value of the given key.

  Example:
  (-> (.once bacon {:a 1})
      (.map (prop :a))
      (.log))
  ;; => 1
  "
  [key]
  (fn [data] (get data key)))

(defn query
  "
  Query selector wrapper. Supports two argument forms:

  1. selector
  Takes selector string
  Returns first element matching the selector within the entire document

  2. container selector
  Takes container DOM element and a selector string
  Returns first element matching the selector within the given container.

  Returns a DOMElement
  "
  ([selector]
   (query js/document selector))
  ([container selector]
   (.querySelector container selector)))

(defn query-all
  "Query selector all wrapper. Supports two argument forms:

  1. selector
  Takes selector string
  Returns seq of elements matching the selector string within entire document.

  2. container selector
  Takes a container DOM element and selector string
  Returns seq of elements matching the selector string within given container.
  "
  ([selector]
   (query-all js/document selector))
  ([container selector]
   (js->clj (.from js/Array (.querySelectorAll container selector)))))
