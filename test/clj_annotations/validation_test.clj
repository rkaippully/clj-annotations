(ns clj-annotations.validation-test
  (:require [clojure.test :refer :all]
            [clj-annotations.core :refer [defschema]]
            [clj-annotations.validation :as sut]))

(defn non-empty-string?
  [s _]
  (cond
    (= s "")                  {:errors ["Value is empty"]}
    (clojure.string/blank? s) {:warnings ["Value is blank"]}))

(defschema website
  :attributes
  {:title
   {:type :string}

   :location
   {:type     :url
    :required true}})

(defschema player
  :attributes
  {:id
   {:type     :uuid
    :required true}

   :name
   {:type     :string
    :required true
    :validity non-empty-string?}

   :wins
   {:type :number}

   :verified?
   {:type     :boolean
    :required true}

   :date-verified
   {:type :date}

   :websites
   {:type         website
    :multi-valued true}

   :level
   {:type             :string
    :canonical-values ["rookie"
                       "intermediate"
                       "master"]}

   :misc
   {:type :bag}

   :unicorn
   {:type :magic}})

(deftest type-check-tests
  (testing "non-map for object"
    (is (= [{:path    ""
             :level   :error
             :kind    :non-map-value
             :message "Expected a map but found long"}] (sut/validate-object player 42))))

  (testing "array for multivalued attribute"
    (is (= [] (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                           :name      "Raghu"
                                           :verified? true
                                           :websites  [{:location "http://www.google.com"}]}))))

  (testing "non-array for vector"
    (is (= [{:path    "/websites"
             :level   :error
             :kind    :non-array-value
             :message "Expected an array value but found a scalar"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true
                                       :websites  42}))))

  (testing "primitive type matches"
    (is (= []
          (sut/validate-object player {:id            "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name          "Raghu"
                                       :verified?     true
                                       :wins          42
                                       :date-verified (java.util.Date. 0)
                                       :misc          {:haskell-is-better true}
                                       :websites      [{:location "http://www.google.com"}]}))))

  (testing "primitive type mismatch"
    (is (= #{{:path    "/id"
              :level   :error
              :kind    :type-mismatch
              :message "Malformed UUID"}
             {:path    "/name"
              :level   :error
              :kind    :type-mismatch
              :message "Expected a string but found long"}
             {:path    "/verified?"
              :level   :error
              :kind    :type-mismatch
              :message "Expected a boolean but found long"}
             {:path    "/wins"
              :level   :error
              :kind    :type-mismatch
              :message "Expected a number but found string"}
             {:path    "/date-verified"
              :level   :error
              :kind    :type-mismatch
              :message "Expected a date but found long"}
             {:path    "/misc"
              :level   :error
              :kind    :type-mismatch
              :message "Expected a map but found long"}
             {:path    "/websites/0/location"
              :level   :error
              :kind    :type-mismatch
              :message "Malformed URL"}}
          (set (sut/validate-object player {:id            42
                                            :name          42
                                            :verified?     42
                                            :wins          "blah"
                                            :date-verified 42
                                            :misc          42
                                            :websites      [{:location 42}]})))))

  (testing "required attributes"
    (is (= [{:path    "/name"
             :level   :error
             :kind    :missing-required-attribute
             :message "Missing required attribute"}
            {:path    "/verified?"
             :level   :error
             :kind    :missing-required-attribute
             :message "Missing required attribute"}
            {:path    "/id"
             :level   :error
             :kind    :missing-required-attribute
             :message "Missing required attribute"}] (sut/validate-object player {:name nil}))))

  (testing "nil value for not required attribute"
    (is (= [] (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                           :name      "Raghu"
                                           :verified? true
                                           :wins      nil}))))

  (testing "unknown attribute type"
    (is (= [{:path    "/unicorn"
             :level   :error
             :kind    :unknown-attribute-type
             :message "Unknown attribute type: :magic"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true
                                       :unicorn   42})))))

(deftest unsupported-attributes-test
  (testing "unsupported attributes"
    (is (= [{:path "/blah" :level :error :kind :unsupported-attribute :message "Unsupported attribute"}]
          (sut/validate-object player {:id        1
                                       :name      "Raghu"
                                       :verified? true
                                       :blah      42}))))

  (testing "disable unsupported attributes"
    (is (= [] (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                           :name      "Raghu"
                                           :verified? true
                                           :blah      42}
                (merge sut/standard-opts {:fail-on-unsupported-attributes? false}))))))

(deftest format-errors-test
  (testing "invalid UUID format"
    (is (= [{:path "/id" :level :error :kind :type-mismatch :message "Malformed UUID"}]
          (sut/validate-object player {:id        "c2a5080c-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true}))))

  (testing "invalid URL format"
    (is (= [{:path "/websites/0/location" :level :error :kind :type-mismatch :message "Malformed URL"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true
                                       :websites  [{:location "blah"}]})))))

(deftest validation-tests
  (testing "warning messages"
    (is (= [{:path "/name" :level :warning :kind :validation-failure :message "Value is blank"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "   "
                                       :verified? true}))))

  (testing "error messages"
    (is (= [{:path "/name" :level :error :kind :validation-failure :message "Value is empty"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      ""
                                       :verified? true}))))

  (testing "canonical values error"
    (is (= [{:path "/level" :level :error :kind :canonical-value-mismatch :message "Must be one of: rookie, intermediate, master"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true
                                       :level     "novice"}))))

  (testing "call make-result with invalid kind"
    (is (= [] (sut/make-validation-result nil nil nil :blah nil)))))
