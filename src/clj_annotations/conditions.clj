(ns clj-annotations.conditions
  "Conditions used in annotations.

  A condition is a function that takes a value and an optional context map as parameters
  and returns a result map. The context map contains extra data that is passed as input. The
  result map will be empty if the conditiong is satisfied (indicating success). Otherwise, it will
  have at least one of the keys `:warnings` and `:errors`. These keys will map to a sequence
  of warning and error message strings respectively."
  (:refer-clojure :exclude [and or])
  (:require
   [clojure.core :as clj])
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
                 (clj/or (contains? res :errors)
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
                 (clj/or (contains? res :errors)
                   (contains? res :warnings)) (c v ctx)
                 :else                        res))]
       (reduce f (cnd v ctx) cnds)))))

(defn seq-length
  [{:keys [lt le gt ge eq ne]} l name]
  (cond
    (clj/and (some? lt) (>= l lt))   {:errors [(str name " should be less than " lt)]}
    (clj/and (some? le) (> l le))    {:errors [(str name " should be less than or equal to " le)]}
    (clj/and (some? gt) (<= l gt))   {:errors [(str name " should be greater than " gt)]}
    (clj/and (some? ge) (< l ge))    {:errors [(str name " should be greater than or equal to " ge)]}
    (clj/and (some? ne) (= l ne))    {:errors [(str name " should not be equal to " ne)]}
    (clj/and (some? eq) (not= l eq)) {:errors [(str name " should be equal to " eq)]}
    :else                            {}))

(defn string-length
  ""
  [opts ^String s _]
  (seq-length opts (if s (.length s) 0) "String length"))

(defn array-length
  [opts xs _]
  (seq-length opts (count xs) "Array length"))

(defn unique-attribute?
  "Takes a sequence of maps and ensure that the value of attribute `k' is unique across
  all the maps."
  [k ms _]
  (let [g (->> (group-by #(get % k) ms)
               (map #(count (second %))))]
    (if (every? #(= % 1) g)
      {}
      {:errors [(str "Duplicate values for the attribute " k)]})))

(defn valid-url?
  [s _]
  (try
    (URL. s)
    {}
    (catch MalformedURLException e
      {:errors ["Malformed URL"]})))

(defn regex-match?
  [re s _]
  (if (clj/and re s (re-matches re s))
    {}
    {:errors [(str "Value must match the regular expression: " re)]}))
