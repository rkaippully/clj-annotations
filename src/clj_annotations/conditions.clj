(ns clj-annotations.conditions
  "Conditions used in annotations.

  A condition is a function that takes a value and an optional context map as parameters
  and returns a result map. The context map contains extra data that is passed as input. The
  result map will be empty if the conditiong is satisfied (indicating success). Otherwise, it will
  have at least one of the keys `:warnings` and `:errors`. These keys will map to a sequence
  of warning and error message strings respectively."
  (:import
   [java.net URL MalformedURLException]))

(defmacro and
  "Evaluates conditions one at a time, from left to right. If a condition returns an
  error result, `and` returns that value and doesn't evaluate any of the other expressions,
  otherwise it returns the value of the last expr."
  ([x] x)
  ([x & next]
   `(let [and# ~x]
      (cond
        (contains? and# :errors)   and#
        (contains? and# :warnings) (merge-with concat and# (and ~@next))
        :else                      (and ~@next)))))

(defmacro or
  "Evaluates conditions one at a time, from left to right. If a condition returns a
  success value, `or` returns that value and doesn't evaluate any of the other expressions, otherwise it
  returns the value of the last expression."
  ([x] x)
  ([x & next]
   `(let [or# ~x]
      (if (contains? or# :success) or# (or ~@next)))))

(defn seq-length
  [{:keys [lt le gt ge eq ne]} l name]
  (cond
    (and (some? lt) (>= l lt))   {:error (str name " should be less than " lt)}
    (and (some? le) (> l le))    {:error (str name " should be less than or equal to " le)}
    (and (some? gt) (<= l gt))   {:error (str name " should be greater than " gt)}
    (and (some? ge) (< l ge))    {:error (str name " should be greater than or equal to " ge)}
    (and (some? eq) (not= l eq)) {:error (str name " should be equal to " eq)}
    (and (some? ne) (= l ne))    {:error (str name " should not be equal to " ne)}
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
      {:error (str "Duplicate values for the attribute " k)})))

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

