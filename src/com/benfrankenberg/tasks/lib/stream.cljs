(ns src.com.benfrankenberg.tasks.lib.stream)

(def stream (js/require "@eccentric-j/highland"))

(defn consume-observer
  [{on-error :error on-complete :complete on-next :next}]
  (fn [err x push next]
    (let [o {:next #(push nil %)
             :error #(push %)
             :complete #(push nil end)
             :continue #(next)
             :value x
             :err err}]
      (cond (and err on-error)          (on-error o)
            err                         (do (push err)
                                            (next))
            (and (= x end) on-complete) (on-complete o)
            (= x end)                   (push nil x)
            on-next                     (on-next o)
            :else                       (do (push nil x)
                                            (next))))))

(def end (.-nil stream))

(defn end?
  [x]
  (= x end))

(defn err?
  [err]
  (not (nil? err)))

(defn merge
  [source sources]
  (-> (stream (clj->js (conj sources source)))
      (.merge)))

(defn take-until
  [close-stream]
  (let [open? (atom true)
        ended? (atom false)]
    (fn [err x push next]
      (when open?
        (.pull close-stream
           (fn []
            (when-not ended?
              (reset! ended? true)
              (push nil end))))
        (reset! open? false))
      (cond (err? err)   (do (push err)
                             (next))
            (end? x)     (do (reset! ended? true)
                             (.destroy close-stream)
                             (push nil x))
            (not ended?) (do (push nil x)
                             (next))))))

(defn take-while
  [f]
  (let [ended (atom false)]
    (fn [err x push next]
      (cond (err? err)   (do (push err)
                             (reset! ended true))
            (end? x)     (do (reset! ended true)
                             (push nil x))
            (not @ended) (if (f x)
                           (do (push nil x)
                               (next))
                           (do (push nil x)
                               (push nil end)
                               (reset! ended true)))))))


