(ns clj-annotations.conditions
  "Conditions used in annotations.

  A condition is a function that takes a value and an optional context map as parameters
  and returns a result map. The context map contains extra data that is passed as input. The
  result map will be empty if the conditiong is satisfied (indicating success). Otherwise, it will
  have at least one of the keys `:warnings` and `:errors`. These keys will map to a sequence
  of warning and error message strings respectively."
  (:refer-clojure :exclude [and or])
  (:import
   [java.net URL MalformedURLException]))

(defn and
  "Evaluates conditions one at a time, from left to right. If a condition returns an
  error or warning result, `and` returns that value and doesn't evaluate any of the
  other conditions, otherwise it returns the result of the last condition."
  [cnd & cnds]
  (fn this
    ([v]
     (this v {}))
    ([v ctx]
     (letfn [(f [res c]
               (cond
                 (clojure.core/or (contains? res :errors)
                   (contains? res :warnings)) res
                 :else                        (c v ctx)))]
       (reduce f (cnd v ctx) cnds)))))

(defn or
  "Evaluates conditions one at a time, from left to right. If a condition returns a
  success value, `or` returns that value and doesn't evaluate any of the other
  conditions, otherwise it returns the value of the last condition"
  [cnd & cnds]
  (fn this
    ([v]
     (this v {}))
    ([v ctx]
     (letfn [(f [res c]
               (cond
                 (clojure.core/or (contains? res :errors)
                   (contains? res :warnings)) (c v ctx)
                 :else                        res))]
       (reduce f (cnd v ctx) cnds)))))

(defn seq-length
  [{:keys [lt le gt ge eq ne]} l name]
  (cond
    (and (some? lt) (>= l lt))   {:errors [(str name " should be less than " lt)]}
    (and (some? le) (> l le))    {:errors [(str name " should be less than or equal to " le)]}
    (and (some? gt) (<= l gt))   {:errors [(str name " should be greater than " gt)]}
    (and (some? ge) (< l ge))    {:errors [(str name " should be greater than or equal to " ge)]}
    (and (some? eq) (not= l eq)) {:errors [(str name " should be equal to " eq)]}
    (and (some? ne) (= l ne))    {:errors [(str name " should not be equal to " ne)]}
    :else                        {}))

(defn string-length
  ""
  [opts ^String s]
  (seq-length opts (.length s) "String length"))

(defn array-length
  [opts xs]
  (seq-length opts (count xs) "Array length"))

(defn unique-attribute?
  "Takes a sequence of maps and ensure that the value of attribute `k' is unique across
  all the maps."
  [k ms]
  (let [vs (set (map #(get % k) ms))]
    (if (= (count ms) (count vs))
      {}
      {:errors [(str "Duplicate values for the attribute " k)]})))

(defn valid-url?
  [s]
  (try
    (URL. s)
    {}
    (catch MalformedURLException e
      {:errors ["Malformed URL"]})))

(defn regex-match?
  [re s]
  (if (re-matches re s)
    {}
    {:errors [(str "Value must match the regular expression: " re)]}))

