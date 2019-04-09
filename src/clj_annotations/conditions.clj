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
  "Forms a compound condition that evaluates the given conditions one at a time, from left
  to right. If a condition returns an error or warning result, `and` returns that value
  and doesn't evaluate any of the other conditions, otherwise it returns the result of the
  last condition."
  [cnd & cnds]
  (fn [v ctx]
    (letfn [(f [res c]
              (cond
                (clj/or
                  (contains? res :errors)
                  (contains? res :warnings)) res
                :else                        (c v ctx)))]
      (reduce f (cnd v ctx) cnds))))

(defn or
  "Forms a compound condition that evaluates conditions one at a time, from left to
  right. If a condition returns a success value, `or` returns that value and doesn't
  evaluate any of the other conditions, otherwise it returns the value of the last
  condition"
  [cnd & cnds]
  (fn [v ctx]
    (letfn [(f [res c]
              (cond
                (clj/or
                  (contains? res :errors)
                  (contains? res :warnings)) (c v ctx)
                :else                        res))]
      (reduce f (cnd v ctx) cnds))))

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
  "`(string-length opts)` returns a condition to check if length of a string matches certain
  criteria. `opts` is a map specifying the match criteria and can have the following keys:

  :lt - string length must be less than the value of this key
  :le - string length must be less than or equal to the value of this key
  :gt - string length must be greater than the value of this key
  :ge - string length must be greater than or equal to the value of this key
  :eq - string length must be equal to the value of this key
  :ne - string length must not be equal to the value of this key"
  [opts]
  (fn [s _]
    (seq-length opts (if s (.length s) 0) "String length")))

(defn coll-length
  "`(coll-length opts)` returns a condition to check if length of a collection matches
  certain criteria. `opts` is a map specifying the match criteria and can have the
  following keys:

  :lt - collection length must be less than the value of this key
  :le - collection length must be less than or equal to the value of this key
  :gt - collection length must be greater than the value of this key
  :ge - collection length must be greater than or equal to the value of this key
  :eq - collection length must be equal to the value of this key
  :ne - collection length must not be equal to the value of this key"
  [opts]
  (fn [xs _]
    (seq-length opts (count xs) "Collection length")))

(defn unique-attribute?
  "`(unique-attribute k)` returns a condition that takes a sequence of maps `ms` and ensure
  that the value of attribute `k` is unique across all the maps."
  [k]
  (fn [ms _]
    (let [g (map #(count (second %)) (group-by #(get % k) ms))]
      (if (every? #(= % 1) g)
        {}
        {:errors [(str "Duplicate values for the attribute " k)]}))))

(defn regex-match?
  "`(regex-match re)` returns a condition that takes a string `s` as input and checks if
  it matches the regular expression `re`"
  [re]
  (fn [s _]
    (if (clj/and re s (re-matches re s))
      {}
      {:errors [(str "Value must match the regular expression: " re)]})))
