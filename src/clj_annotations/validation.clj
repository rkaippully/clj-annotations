(ns clj-annotations.validation
  "Validations based on annotations."
  (:require
   [clojure.string :as str]
   [clojure.set :as cs]
   [clj-annotations.core :as core]
   [clj-annotations.conditions :as cond])
  (:import
   [java.net URL MalformedURLException]
   [java.util Date UUID]))

(defn type-name
  "Returns the simple name of the class of `x`. For e.g., (type-name true) will return
  \"boolean\"."
  [x]
  (str/lower-case (.getSimpleName (class x))))

(defn valid-url?
  "Returns logical true if `s` is a valid URL string"
  [s]
  (try
    (URL. s)
    (catch MalformedURLException _
      nil)))

(defn valid-uuid?
  "Returns logical true if string `s` is a valid UUID in its canonical textual
  representation."
  [s]
  (try
    (UUID/fromString s)
    (catch IllegalArgumentException _
      nil)))

(def type-checks
  "A standard set of type validations used by `validate-object`. This includes validation
  for `:string`, `:boolean`, `:number`, `:date`, `:uuid`, `:url`, and `:bag` types."
  (letfn [(f [check msg]
            (if check
              {}
              {:errors [{:kind :type-mismatch :message msg}]}))]
    {:string  #(f (instance? String %2) (str "Expected a string but found " (type-name %2)))
     :boolean #(f (instance? Boolean %2) (str "Expected a boolean but found " (type-name %2)))
     :number  #(f (instance? Number %2) (str "Expected a number but found " (type-name %2)))
     :date    #(f (instance? Date %2) (str "Expected a date but found " (type-name %2)))
     :uuid    #(f (and (string? %2) (valid-uuid? %2)) "Malformed UUID")
     :url     #(f (and (string? %2) (valid-url? %2)) "Malformed URL")
     :bag     #(f (map? %2) (str "Expected a map but found " (type-name %2)))}))

(defn make-validation-result
  "Returns a standard validation result.
  `schema` - the schema of the attribute. optional.
  `value` - the value of the attribute. optional
  `path` - a sequence of attribute ids representing the location where the validation
  failed.
  `result` - either the output of a condition or one of the keywords below respresenting a
  validation error."
  [schema value path result]
  (letfn [(cond->result [level res]
            (let [res' (if (string? res)
                         {:kind :validation-failure :message res}
                         res)]
              (assoc res' :path (reduce #(str %1 "/" %2) "" path) :level level)))
          (error->result [kind msg]
            [(cond->result :error {:kind    kind
                                   :message msg})])]
    (case result
      :unknown-attribute-type     (error->result :unknown-attribute-type (str "Unknown attribute type: " (:type schema)))
      :canonical-value-mismatch   (error->result :canonical-value-mismatch (str "Must be one of: " (str/join ", " (:canonical-values schema))))
      :non-array-value            (error->result :non-array-value "Expected an array value but found a scalar")
      :non-map-value              (error->result :non-map-value (str "Expected a map but found " (type-name value)))
      :missing-required-attribute (error->result :missing-required-attribute "Missing required attribute")
      :unsupported-attribute      (error->result :unsupported-attribute "Unsupported attribute")
      ;; this is a condition result if none of the above match
      (concat
        (map #(cond->result :error %) (:errors result))
        (map #(cond->result :warning %) (:warnings result))))))

(def standard-opts
  "Standard options passed to `validate-object` if not overridden."
  {:type-checks                     type-checks
   :make-result                     make-validation-result
   :fail-on-unsupported-attributes? true
   :validation-fns
   {}})

(defn- eval-validity-condition
  [path schema obj {:keys [make-result]}]
  (let [validity-fn (:validity schema)
        ctx {:path path}]
    (if-let [result (and validity-fn (validity-fn obj ctx))]
      (make-result schema obj path result)
      [])))

(declare validate-object)

(defn- validate-scalar-attribute
  [path schema obj {:keys [type-checks make-result validation-fns] :as opts}]
  (let [typ               (:type schema)
        canon-vals        (:canonical-values schema)
        type-check-result (when-let [f (get type-checks typ)]
                            (f schema obj))]
    (cond
      ;; Type checks
      (and (= (type typ) ::core/schema))
      (validate-object path typ obj opts)

      (and (contains? type-checks typ) (seq type-check-result))
      (make-result schema obj path type-check-result)

      (not (contains? (set (keys type-checks)) typ))
      (make-result schema obj path :unknown-attribute-type)

      ;; Validation
      (and canon-vals (not-any? (set canon-vals) [obj]))
      (make-result schema obj path :canonical-value-mismatch)

      :else
      (eval-validity-condition path schema obj opts))))

(defn- validate-vector-attribute
  [path schema obj {:keys [make-result validation-fns] :as opts}]
  (if (sequential? obj)
    (letfn [(validate [i v]
              (validate-scalar-attribute (conj path i) schema v opts))]
     (apply concat (map-indexed validate obj)))
    (make-result schema obj path :non-array-value)))

(defn- validate-attribute
  [path
   {:keys [required multi-valued] :or {required false multi-valued false} :as schema}
   obj
   {:keys [make-result] :as opts}]
  (cond
    (and required (= obj ::not-found))       (make-result schema obj path :missing-required-attribute)
    (and (not required) (= obj ::not-found)) []
    (and (not required) (nil? obj))          []
    multi-valued                             (validate-vector-attribute path schema obj opts)
    :else                                    (validate-scalar-attribute path schema obj opts)))

(defn validate-object
  "Validate an object given its schema. Returns a sequence of as produced by the
  `make-result` function. Returns an empty sequence if there are no validation
  errors/warnings.

  `path`   - a vector of the path elements to be used in error messages. Use [] at top
  level.
  `schema` - the schema to validate against.
  `obj`    - the object to validate
  `opts`   - options for validation, this should be a map with the following keys

  `:make-result` - a function that takes the schema, the attribute value, the attribute
  path, and a validation result and returns a validation result as a vector. See
  `make-validation-result` for an example.
  `:type-checks` - a map from type names to functions validating the conformance of a
  value to those types. See `type-checks` for an example.
  `:fail-on-unsupported-attributes?` - if true an error will be reported if the `obj`
  contains an attribute that is not defined in `schema`."
  ([schema obj]
   (validate-object schema obj standard-opts))
  ([schema obj opts]
   (validate-object [] schema obj opts))
  ([path schema obj {:keys [make-result fail-on-unsupported-attributes?] :as opts}]
   (if (map? obj)
     (let [attrs             (:attributes schema)
           obj-attrs         (set (keys obj))
           schema-attrs      (set (keys attrs))
           unsupported-attrs (map name (cs/difference obj-attrs schema-attrs))]
       (if (and fail-on-unsupported-attributes? (seq unsupported-attrs))
         (mapcat #(make-result schema obj (conj path (name %)) :unsupported-attribute) unsupported-attrs)
         (mapcat #(validate-attribute (conj path (name %)) (get attrs %) (get obj % ::not-found) opts)
           schema-attrs)))
     (make-result schema obj path :non-map-value))))

