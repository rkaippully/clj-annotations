(ns clj-annotations.validation-test
  (:require [clojure.test :refer :all]
            [clj-annotations.core :refer [defschema]]
            [clj-annotations.validation :as sut]
            [clj-annotations.conditions :as c]))

(defn non-empty-string?
  [s _]
  (cond
    (= s "")                  {:errors ["Value is empty"]}
    (clojure.string/blank? s) {:warnings ["Value is blank"]}))

(defn unique-values?
  [xs _]
  (when-not (apply distinct? xs)
    {:errors ["Duplicate values"]}))

(defn contender?
  [player _]
  (when-not (:verified? player)
    {:errors ["Contenders must be verified"]}))

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

   :emails
   {:type :string
    :multi-valued true
    :validity unique-values?}

   :websites
   {:type         website
    :multi-valued true
    :validity     (c/and (c/coll-length {:le 2})
                         (c/unique-attribute? :location))}

   :level
   {:type             :string
    :canonical-values ["rookie"
                       "intermediate"
                       "master"]}

   :misc
   {:type :bag}

   :unicorn
   {:type :magic}})

(defschema championship
  :attributes
  {:player1
   {:type     player
    :validity contender?}

   :player2
   {:type     player
    :validity contender?}})

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
                                           :websites  [{:title "Google" :location "http://www.google.com"}]}))))

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
    (is (= [] (sut/make-validation-result nil nil nil :blah nil))))

  (testing "nil values in multi-valued attributes"
    (is (= [{:path    "/websites/0"
             :level   :error
             :kind    :non-map-value
             :message "Expected a map but found nil"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true
                                       :websites  [nil]}))))

  (testing "validation of simple multi-valued attributes"
    (let [base-player {:id "c2a5080c-d09b-49c7-baa9-38602235c9c5" :name "Raghu" :verified? true}
          good-emails ["foo@bar.baz" "abc@def.ghi" "coolguy@test.com"]
          bad-emails ["foo@bar.baz" "abc@def.ghi" "foo@bar.baz"]]
      (is (= []
             (sut/validate-object player (assoc base-player :emails good-emails))))
      (is (= [{:path    "/emails"
               :level   :error
               :kind    :validation-failure
               :message "Duplicate values"}]
             (sut/validate-object player (assoc base-player :emails bad-emails))))))

  (testing "validation of complex multi-valued attributes"
    (is (= [{:path    "/websites"
             :level   :error
             :kind    :validation-failure
             :message "Collection length should be less than or equal to 2"}
            {:path    "/websites/2/location"
             :level   :error
             :kind    :type-mismatch
             :message "Malformed URL"}]
          (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                       :name      "Raghu"
                                       :verified? true
                                       :websites  [{:location "http://www.google.com"}
                                                   {:location "http://www.github.com"}
                                                   {:location ""}]}))))

  (testing "uniqueness on multi-valued attributes"
    (is (= [{:path    "/websites"
             :level   :error
             :kind    :validation-failure
             :message "Duplicate values for the attribute :location"}]
           (sut/validate-object player {:id        "c2a5080c-d09b-49c7-baa9-38602235c9c5"
                                        :name      "Raghu"
                                        :verified? true
                                        :websites  [{:location "http://www.google.com"}
                                                    {:location "http://www.google.com"}]}))))

  (testing "validation of schema object attributes"
    (let [id "c2a5080c-d09b-49c7-baa9-38602235c9c5"
          good-contender {:id id :name "Raghu" :level "master" :verified? true}
          bad-contender {:id id :name "Raghu" :level "master" :verified? false}
          error-player {:id id :name "Raghu" :level "master"}
          warning-player1 {:id id :name " " :level "master" :verified? false}
          warning-player2 {:id id :name " " :level "master" :verified? true}]
      ;; all checks pass
      (is (= (sut/validate-object championship {:player1 good-contender :player2 good-contender})
             []))
      ;; contender check fails
      (is (= (sut/validate-object championship {:player1 good-contender :player2 bad-contender})
             [{:path "/player2" :level :error :kind :validation-failure
               :message "Contenders must be verified"}]))
      ;; contender check is not run if the player has errors
      (is (= (sut/validate-object championship {:player1 good-contender :player2 error-player})
             [{:path "/player2/verified?" :level :error :kind :missing-required-attribute
               :message "Missing required attribute"}]))
      ;; contender check is run if the player has warnings
      (is (= (sut/validate-object championship {:player1 good-contender :player2 warning-player1})
             [{:path "/player2/name" :level :warning :kind :validation-failure
               :message "Value is blank"}
              {:path "/player2" :level :error :kind :validation-failure
               :message "Contenders must be verified"}]))
      ;; player warnings are still reported if the contender check passes
      (is (= (sut/validate-object championship {:player1 good-contender :player2 warning-player2})
             [{:path "/player2/name" :level :warning :kind :validation-failure
               :message "Value is blank"}])))))
