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

(sut/defschema company
  :attributes
  {:id   {:type :integer}
   :name {:type :string}})

(sut/defschema employee
  :include    user
  :attributes
  {:id
   {:type :string}
   :employeeNumber
   {:type :string}
   :company
   {:type company}})

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
    (is (= #{:id :name :employeeNumber :company} (set (keys (sut/get-annotations employee))))))

  (testing "include overrides attributes"
    (is (= :string (sut/get-annotations employee :id :type))))

  (testing "include multiple schemas"
    (let [null-id  (sut/make-schema {:attributes {:id {:nullable true}}})
          employee (sut/make-schema {:include    [user null-id]
                                     :attributes {:id {:type :string}}})]
      (is (= employee {:attributes
                       {:id
                        {:type     :string
                         :nullable true}
                        :name
                        {:type     :string
                         :validity :non-empty}}}))))

  (testing "make-schema removes :include"
    (is (not (contains? employee :include)))))

(deftest get-annotations-test
  (testing "all attributes"
    (is (= #{:id :name} (set (keys (sut/get-annotations user))))))
  (testing "properties of an attribute"
    (is (= {:type :integer :nullable false} (sut/get-annotations user :id))))
  (testing "a specific property"
    (is (= :integer (sut/get-annotations user :id :type))))
  (testing "a specific property with not-found value"
    (is (= :not-found (sut/get-annotations user :id :blah :not-found))))
  (testing "nested attributes"
    (is (= :string (sut/get-annotations employee [:company :name] :type)))))

(deftest scan-attributes-test
  (testing "scan-attributes"
    (is (= #{[:id] [:name] [:employeeNumber] [:company :name]} (sut/scan-attributes employee :type #(= % :string))))))
