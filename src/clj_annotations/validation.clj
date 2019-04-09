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

(def standard-type-checks
  "A standard set of type validations used by `validate-object`. This includes validation
  for `:string`, `:boolean`, `:number`, `:date`, `:uuid`, `:url`, and `:bag` types."
  {:string  #(when-not (instance? String %2)
               (str "Expected a string but found " (type-name %2)))
   :boolean #(when-not (instance? Boolean %2)
               (str "Expected a boolean but found " (type-name %2)))
   :number  #(when-not (instance? Number %2)
               (str "Expected a number but found " (type-name %2)))
   :date    #(when-not (instance? Date %2)
               (str "Expected a date but found " (type-name %2)))
   :uuid    #(if (string? %2)
               (when-not (valid-uuid? %2)
                 "Malformed UUID")
               (str "Expected a UUID but found " (type-name %2)))
   :url     #(if (string? %2)
               (when-not (valid-url? %2)
                 "Malformed URL")
               (str "Expected a URL but found " (type-name %2)))
   :bag     #(when-not (map? %2)
               (str "Expected a map but found " (type-name %2)))})

(defn make-validation-result
  "Returns a standard validation result"
  [path level msg]
  [{:path    (reduce #(str %1 "/" %2) "" path)
    :level   level
    :message msg}])

(def standard-opts
  "Standard options passed to `validate-object` if not overridden."
  {:type-checks                     standard-type-checks
   :make-result                     make-validation-result
   :fail-on-unsupported-attributes? true})

(defn- eval-validity-condition
  [path schema obj {:keys [make-result]}]
  (let [validity-fn (:validity schema)
        ctx {:path path}]
    (if-let [res (and validity-fn (validity-fn obj ctx))]
      (concat
        (mapcat #(make-result path :error %) (:errors res))
        (mapcat #(make-result path :warning %) (:warnings res)))
      [])))

(declare validate-object)

(defn- validate-scalar-attribute
  [path schema obj {:keys [type-checks make-result] :as opts}]
  (let [typ               (:type schema)
        canon-vals        (:canonical-values schema)
        type-check-result (when-let [f (get type-checks typ)]
                            (f schema obj))]
    (cond
      ;; Type checks
      (and (= (type typ) ::core/schema))                  (validate-object path typ obj opts)
      (and (contains? type-checks typ) type-check-result) (make-result path :error type-check-result)
      (not (contains? (set (keys type-checks)) typ))      (make-result path :error (str "Unknown attribute type: " typ))

      ;; Validation
      (and canon-vals (not-any? (set canon-vals) [obj])) (make-result path :error (str "Must be one of: " (str/join ", " canon-vals)))
      :else                                              (eval-validity-condition path schema obj opts))))

(defn- validate-vector-attribute
  [path schema obj {:keys [make-result] :as opts}]
  (if (sequential? obj)
    (letfn [(validate [i v]
              (validate-scalar-attribute (conj path i) schema v opts))]
     (apply concat (map-indexed validate obj)))
    (make-result path :error "Expected an array value but found a scalar")))

(defn- validate-attribute
  [path
   {:keys [required multi-valued] :or {required false multi-valued false} :as schema}
   obj
   {:keys [make-result] :as opts}]
  (cond
    (and required (= obj ::not-found))       (make-result path :error "Missing required attribute")
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

  `:make-result` - a function that takes the path, message level, and a message and
  returns a validation result as a vector. See `make-validation-result` for an example.
  `:type-checks` - a map from type names to functions validating the conformance of a
  value to those types. See `standard-type-checks` for an example.
  `:fail-on-unsupported-attributes?` - if true an error will be reported if the `obj`
  contains an attribute that is not defined in `schema`."
  ([schema obj]
   (validate-object schema obj standard-opts))
  ([schema obj opts]
   (validate-object [] schema obj opts))
  ([path schema obj {:keys [make-result fail-on-unsupported-attributes?] :as opts}]
   (if (map? obj)
     (let [attrs              (:attributes schema)
           obj-attrs          (set (keys obj))
           schema-attrs       (set (keys attrs))
           unsupported-attrs (map name (cs/difference obj-attrs schema-attrs))]
       (if (and fail-on-unsupported-attributes? (seq unsupported-attrs))
         (mapcat #(make-result (conj path (name %)) :error "Unsupported attribute") unsupported-attrs)
         (mapcat #(validate-attribute (conj path (name %)) (get attrs %) (get obj % ::not-found) opts)
           schema-attrs)))
     (make-result path :error (str "Expected a map but found " (type-name obj))))))

