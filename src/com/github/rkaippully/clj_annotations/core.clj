(ns com.github.rkaippully.clj-annotations.core
  "Access and manipulate annotations.

  Annotations are arbitrary properties associated with values that can be used for a variety
  of use cases. Typically you define a \"type\" using `defannot` specifying its attributes.
  Each of the types and attributes can have arbitrary properties - key/value pairs - associated
  with them.

  This namespace provides functions and macros to define and access annotations.")


;;
;; Annotation accessor functions
;;

(defn get-annotations
  "Access the annotations associated with a type, an attribute, and a property. Returns all
  properties if only a type and attribute are provided. Returns nil if the specified
  property does not exist, or the not-found value if supplied."
  ([type attr]
   (get-in type [:attributes attr]))
  ([type attr prop]
   (get-in type [:attributes attr prop]))
  ([type attr prop not-found]
   (get-in type [:attributes attr prop] not-found)))

(defn get-attributes
  "Fetch a sequence of attribute ids of a type having a specified property set to the
  specified value."
  [type prop value]
  (letfn [(prop-check [[attr props]]
            (= (get props prop) value))]
    (map first (filter prop-check (:attributes type)))))


;;
;; Annotation definition functions
;;

(defn merge-attrs
  "a1 and a2 are annotation attribute maps of the form {:field1 m1, field2 m2, ...}
  and {:field1 n1, :field2 n2, ...}. merge-attrs produces a map of the form
  {:field1 (merge m1 n1), :field2 (merge m2 n2), ...}. If a field exists only in one map,
  that value is used."
  [a1 a2]
  (merge-with merge a1 a2))

(defn make-annotation
  [{:keys [include attributes] :as m}]
  (let [attrs (merge-attrs (:attributes include) attributes)]
    (-> (merge include m)
        (dissoc :include)
        (assoc :attributes attrs)
        (with-meta {:type ::annotation}))))

(defmacro defannot
  "Define annotations for a type.

  `kvs` are key-value pairs. The following keys are defined:

  `:include`:     Include the kvs from another type to this type. If a key occurs in both
                  the types, the one in this type wins.
  `:name`:        A short name for this type used for documentation purposes.
  `:description`: A description for this type used for documentation purposes.
  `:attributes`:  A map from attribute IDs to attribute annotations."
  [name & kvs]
  `(defonce ~name (make-annotation (assoc {} ~@kvs))))

