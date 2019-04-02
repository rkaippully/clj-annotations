(ns clj-annotations.core-test
  (:require [clojure.test :refer :all]
            [clj-annotations.core :as sut]))

(sut/defschema user
  :name        "user"
  :description "The user schema"
  :attributes
  {:id
   {:type     :integer
    :nullable false}

   :name
   {:type     :string
    :validity :non-empty}})

(deftest defschema-test
  (testing "the defschema macro"
    (is (= user {:name         "user"
                 :description "The user schema"
                 :attributes
                 {:id
                  {:type     :integer
                   :nullable false}

                  :name
                  {:type     :string
                   :validity :non-empty}}}))))

(deftest type-test
  (testing "type of schema"
    (let [schema (sut/make-schema {})]
      (is (= (type schema) ::sut/schema)))))

(deftest include-test
  (testing "include merges attribute maps"
    (let [employee (sut/make-schema {:include    user
                                     :attributes {:employeeNumber {:type :string}}})]
      (is (= #{:id :name :employeeNumber} (set (keys (sut/get-annotations employee)))))))

  (testing "include overrides attributes"
    (let [employee (sut/make-schema {:include    user
                                     :attributes {:id {:type :string}}})]
      (is (= :string (sut/get-annotations employee :id :type)))))

  (testing "make-schema removes :include"
    (let [employee (sut/make-schema {:include    user
                                     :attributes {:id {:type :string}}})]
      (is (not (contains? employee :include))))))

(deftest get-annotations-test
  (testing "all attributes"
    (is (= #{:id :name} (set (keys (sut/get-annotations user))))))
  (testing "properties of an attribute"
    (is (= {:type :integer :nullable false} (sut/get-annotations user :id))))
  (testing "a specific property"
    (is (= :integer (sut/get-annotations user :id :type))))
  (testing "a specific property with not-found value"
    (is (= :not-found (sut/get-annotations user :id :blah :not-found)))))

(deftest scan-attributes-test
  (testing "scan-attributes"
    (let [employee (sut/make-schema {:include    user
                                     :attributes {:employeeNumber {:type :string}}})]
      (is (= #{:name :employeeNumber} (set (sut/scan-attributes employee :type :string)))))))
