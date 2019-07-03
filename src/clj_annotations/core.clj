(ns clj-annotations.core
  "Access and manipulate annotations.

  Annotations are arbitrary properties associated with values that can be used for a
  variety of use cases. Typically you define a \"schema\" using `defschema` specifying its
  attributes Each of the schemas and attributes can have arbitrary properties - key/value
  pairs - associated with them.

  This namespace provides functions and macros to define and access annotations."
  (:require [clojure.set :as set]))


;;
;; Annotation accessor functions
;;

(defn make-attr-path
  [attr]
  (as-> attr $attr
    (if (sequential? $attr) $attr [$attr])
    (interpose [:type :attributes] $attr)
    (flatten $attr)
    (concat [:attributes] $attr)))

(defn get-annotations
  "Access the annotations associated with a schema, an attribute, and a property. Returns
  all properties if only `schema` and `attribute` are provided. Returns `nil` if the
  specified property does not exist, or the `not-found` value if supplied. `attr` can be a
  sequential which allows to access nested schema objects."
  ([schema]
   (:attributes schema))
  ([schema attr]
   (get-in schema (make-attr-path attr)))
  ([schema attr prop]
   (get-annotations schema attr prop nil))
  ([schema attr prop not-found]
   (get-in schema (concat (make-attr-path attr) [prop]) not-found)))

(defn scan-attributes
  "Fetch a set of paths of a schema having a specified property satisfying predicate
  `pred`. Each element of the set is a sequence of attribute ids."
  [schema prop pred]
  (letfn [(prop-check [path]
            (fn [acc [attr props]]
              (as-> acc $acc
                (if (pred (get props prop))
                  (conj $acc (conj path attr))
                  $acc)
                (if (= (type (:type props)) ::schema)
                  (-> (prop-check (conj path attr))
                      (reduce #{} (get-in props [:type :attributes]))
                      (set/union $acc))
                  $acc))))]
    (reduce (prop-check []) #{} (:attributes schema))))


;;
;; Annotation definition functions
;;

(defn merge-attrs
  "a1 and a2 are annotation attribute maps of the form {:attr1 m1, :attr2 m2, ...} and
  {:attr1 n1, :attr2 n2, ...}. merge-attrs produces a map of the form {:attr1 (merge m1
  n1), :attr2 (merge m2 n2), ...}. If an attribute exists only in one map, that value is
  used."
  [a1 a2]
  (merge-with merge a1 a2))

(defn make-schema
  [{:keys [include attributes] :as m}]
  (let [attrs        (cond
                       (nil? include)        [attributes]
                       (sequential? include) (conj (vec (map :attributes include)) attributes)
                       :else                 [(:attributes include) attributes])
        merged-attrs (reduce merge-attrs attrs)]
    (-> (dissoc m :include)
        (assoc :attributes merged-attrs)
        (with-meta {:type ::schema}))))

(defmacro defschema
  "Define a schema and binds it to `name`.

  `kvs` are key-value pairs. The following keys are defined:

  `:include`:     Specifies another schema or a collection of schemas to be merged in to
                  this schema. `:include s1` will merge all properties of s1 in to this
                  schema, properties specified in this schema wins in case of
                  conflicts. Similarly, `:include [s1 s2]` will merge all properties from
                  s1 and s2 in to this schema. Properties of `s1`, `s2`, and this schema
                  will win in that order in case of conflicts.
  `:name`:        A short name for this schema used for documentation purposes.
  `:description`: A description for this schema used for documentation purposes.
  `:attributes`:  A map from attribute IDs to attribute annotations."
  [name & kvs]
  `(defonce ~name (make-schema (assoc {} ~@kvs))))
