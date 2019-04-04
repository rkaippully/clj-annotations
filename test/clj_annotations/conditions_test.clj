(ns clj-annotations.conditions-test
  (:require [clojure.test :refer :all]
            [clj-annotations.conditions :as sut]))

(deftest and-test
  (letfn [(cond-success [v & ctx]
            {})
          (cond-warning [n]
            (fn [v & ctx]
              {:warnings [(str "warning" n)]}))
          (cond-error [v & ctx]
            {:errors ["error"]})]

    (testing "and - sss"
      (is (= {} ((sut/and cond-success cond-success cond-success) nil nil))))

    (testing "and - ssw"
      (is (= {:warnings ["warning1"]} ((sut/and cond-success cond-success (cond-warning 1)) nil nil))))

    (testing "and - sse"
      (is (= {:errors ["error"]} ((sut/and cond-success cond-success cond-error) nil nil))))

    (testing "and - sws"
      (is (= {:warnings ["warning1"]} ((sut/and cond-success (cond-warning 1) cond-success) nil nil))))

    (testing "and - sww"
      (is (= {:warnings ["warning1"]} ((sut/and cond-success (cond-warning 1) (cond-warning 2)) nil nil))))

    (testing "and - swe"
      (is (= {:warnings ["warning1"]} ((sut/and cond-success (cond-warning 1) cond-error) nil nil))))

    (testing "and - ses"
      (is (= {:errors ["error"]} ((sut/and cond-success cond-error cond-success) nil nil))))

    (testing "and - sew"
      (is (= {:errors ["error"]} ((sut/and cond-success cond-error (cond-warning 1)) nil nil))))

    (testing "and - see"
      (is (= {:errors ["error"]} ((sut/and cond-success cond-error cond-error) nil nil))))

    (testing "and - wss"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) cond-success cond-success) nil nil))))

    (testing "and - wsw"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) cond-success (cond-warning 2)) nil nil))))

    (testing "and - wse"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) cond-success cond-error) nil nil))))

    (testing "and - wws"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) (cond-warning 2) cond-success) nil nil))))

    (testing "and - www"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) (cond-warning 2) (cond-warning 3)) nil nil))))

    (testing "and - wwe"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) (cond-warning 2) cond-error) nil nil))))

    (testing "and - wes"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) cond-error cond-success) nil nil))))

    (testing "and - wew"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) cond-error (cond-warning 2)) nil nil))))

    (testing "and - wee"
      (is (= {:warnings ["warning1"]} ((sut/and (cond-warning 1) cond-error cond-error) nil nil))))

    (testing "and - ess"
      (is (= {:errors ["error"]} ((sut/and cond-error cond-success cond-success) nil nil))))

    (testing "and - esw"
      (is (= {:errors ["error"]} ((sut/and cond-error cond-success (cond-warning 1)) nil nil))))

    (testing "and - ese"
      (is (= {:errors ["error"]} ((sut/and cond-error cond-success cond-error) nil nil))))

    (testing "and - ews"
      (is (= {:errors ["error"]} ((sut/and cond-error (cond-warning 1) cond-success) nil nil))))

    (testing "and - eww"
      (is (= {:errors ["error"]} ((sut/and cond-error (cond-warning 1) (cond-warning 2)) nil nil))))

    (testing "and - ewe"
      (is (= {:errors ["error"]} ((sut/and cond-error (cond-warning 1) cond-error) nil nil))))

    (testing "and - ees"
      (is (= {:errors ["error"]} ((sut/and cond-error cond-error cond-success) nil nil))))

    (testing "and - eew"
      (is (= {:errors ["error"]} ((sut/and cond-error cond-error (cond-warning 1)) nil nil))))

    (testing "and - eee"
      (is (= {:errors ["error"]} ((sut/and cond-error cond-error cond-error) nil nil))))))

(deftest or-test
  (letfn [(cond-success [v & ctx]
            {})
          (cond-warning [n]
            (fn [v & ctx]
              {:warnings [(str "warning" n)]}))
          (cond-error [n]
            (fn [v & ctx]
              {:errors [(str "error" n)]}))]

    (testing "or - sss"
      (is (= {} ((sut/or cond-success cond-success cond-success) nil nil))))

    (testing "or - ssw"
      (is (= {} ((sut/or cond-success cond-success (cond-warning 1)) nil nil))))

    (testing "or - sse"
      (is (= {} ((sut/or cond-success cond-success (cond-error 1)) nil nil))))

    (testing "or - sws"
      (is (= {} ((sut/or cond-success (cond-warning 1) cond-success) nil nil))))

    (testing "or - sww"
      (is (= {} ((sut/or cond-success (cond-warning 1) (cond-warning 2)) nil nil))))

    (testing "or - swe"
      (is (= {} ((sut/or cond-success (cond-warning 1) (cond-error 1)) nil nil))))

    (testing "or - ses"
      (is (= {} ((sut/or cond-success (cond-error 1) cond-success) nil nil))))

    (testing "or - sew"
      (is (= {} ((sut/or cond-success (cond-error 1) (cond-warning 1)) nil nil))))

    (testing "or - see"
      (is (= {} ((sut/or cond-success (cond-error 1) (cond-error 2)) nil nil))))

    (testing "or - wss"
      (is (= {} ((sut/or (cond-warning 1) cond-success cond-success) nil nil))))

    (testing "or - wsw"
      (is (= {} ((sut/or (cond-warning 1) cond-success (cond-warning 2)) nil nil))))

    (testing "or - wse"
      (is (= {} ((sut/or (cond-warning 1) cond-success (cond-error 1)) nil nil))))

    (testing "or - wws"
      (is (= {} ((sut/or (cond-warning 1) (cond-warning 2) cond-success) nil nil))))

    (testing "or - www"
      (is (= {:warnings ["warning3"]} ((sut/or (cond-warning 1) (cond-warning 2) (cond-warning 3)) nil nil))))

    (testing "or - wwe"
      (is (= {:errors ["error1"]} ((sut/or (cond-warning 1) (cond-warning 2) (cond-error 1)) nil nil))))

    (testing "or - wes"
      (is (= {} ((sut/or (cond-warning 1) (cond-error 1) cond-success) nil nil))))

    (testing "or - wew"
      (is (= {:warnings ["warning2"]} ((sut/or (cond-warning 1) (cond-error 1) (cond-warning 2)) nil nil))))

    (testing "or - wee"
      (is (= {:errors ["error2"]} ((sut/or (cond-warning 1) (cond-error 1) (cond-error 2)) nil nil))))

    (testing "or - ess"
      (is (= {} ((sut/or (cond-error 1) cond-success cond-success) nil nil))))

    (testing "or - esw"
      (is (= {} ((sut/or (cond-error 1) cond-success (cond-warning 1)) nil nil))))

    (testing "or - ese"
      (is (= {} ((sut/or (cond-error 1) cond-success (cond-error 2)) nil nil))))

    (testing "or - ews"
      (is (= {} ((sut/or (cond-error 1) (cond-warning 1) cond-success) nil nil))))

    (testing "or - eww"
      (is (= {:warnings ["warning2"]} ((sut/or (cond-error 1) (cond-warning 1) (cond-warning 2)) nil nil))))

    (testing "or - ewe"
      (is (= {:errors ["error2"]} ((sut/or (cond-error 1) (cond-warning 1) (cond-error 2)) nil nil))))

    (testing "or - ees"
      (is (= {} ((sut/or (cond-error 1) (cond-error 2) cond-success) nil nil))))

    (testing "or - eew"
      (is (= {:warnings ["warning1"]} ((sut/or (cond-error 1) (cond-error 2) (cond-warning 1)) nil nil))))

    (testing "or - eee"
      (is (= {:errors ["error3"]} ((sut/or (cond-error 1) (cond-error 2) (cond-error 3)) nil nil))))))

(deftest string-length-test
  (testing "nil string"
    (is (= {:errors ["String length should be greater than 0"]} (sut/string-length {:gt 0} nil nil))))
  (testing "empty string"
    (is (= {:errors ["String length should be greater than 0"]} (sut/string-length {:gt 0} "" nil))))
  (testing "lt"
    (is (= {:errors ["String length should be less than 3"]} (sut/string-length {:lt 3} "abc" nil))))
  (testing "le"
    (is (= {:errors ["String length should be less than or equal to 3"]} (sut/string-length {:le 3} "abcd" nil))))
  (testing "gt"
    (is (= {:errors ["String length should be greater than 3"]} (sut/string-length {:gt 3} "abc" nil))))
  (testing "ge"
    (is (= {:errors ["String length should be greater than or equal to 3"]} (sut/string-length {:ge 3} "ab" nil))))
  (testing "eq"
    (is (= {:errors ["String length should be equal to 3"]} (sut/string-length {:eq 3} "ab" nil))))
  (testing "ne"
    (is (= {:errors ["String length should not be equal to 3"]} (sut/string-length {:ne 3} "abc" nil))))
  (testing "success"
    (is (= {} (sut/string-length {:ne 3} "ab" nil)))))

(deftest coll-length-test
  (testing "nil array"
    (is (= {:errors ["Collection length should be greater than 0"]} (sut/coll-length {:gt 0} nil nil))))
  (testing "empty array"
    (is (= {:errors ["Collection length should be greater than 0"]} (sut/coll-length {:gt 0} [] nil))))
  (testing "lt"
    (is (= {:errors ["Collection length should be less than 3"]} (sut/coll-length {:lt 3} [1 2 3] nil))))
  (testing "le"
    (is (= {:errors ["Collection length should be less than or equal to 3"]} (sut/coll-length {:le 3} [1 2 3 4] nil))))
  (testing "gt"
    (is (= {:errors ["Collection length should be greater than 3"]} (sut/coll-length {:gt 3} [1 2 3] nil))))
  (testing "ge"
    (is (= {:errors ["Collection length should be greater than or equal to 3"]} (sut/coll-length {:ge 3} [1 2] nil))))
  (testing "eq"
    (is (= {:errors ["Collection length should be equal to 3"]} (sut/coll-length {:eq 3} [1 2] nil))))
  (testing "ne"
    (is (= {:errors ["Collection length should not be equal to 3"]} (sut/coll-length {:ne 3} [1 2 3] nil))))
  (testing "success"
    (is (= {} (sut/coll-length {:ne 3} [1 2] nil)))))

(deftest unique-attribute-test
  (testing "nil sequence"
    (is (= {} (sut/unique-attribute? :a nil nil))))
  (testing "empty sequence"
    (is (= {} (sut/unique-attribute? :a [{}] nil))))
  (testing "single element sequence"
    (is (= {} (sut/unique-attribute? :a [{:a 2}] nil))))
  (testing "no duplicates"
    (is (= {} (sut/unique-attribute? :a [{:a 2 :b 3} {:a 4 :b 3} {:a 5 :c 2}] nil))))
  (testing "with duplicates"
    (is (= {:errors ["Duplicate values for the attribute :a"]} (sut/unique-attribute? :a [{:a 2 :b 3} {:a 4 :b 3} {:a 2 :c 2}] nil)))))

(deftest regex-match-test
  (testing "nil regex"
    (is (= {:errors ["Value must match the regular expression: "]} (sut/regex-match? nil "abc" nil))))
  (testing "nil string"
    (is (= {:errors ["Value must match the regular expression: \\d{4}-\\d{2}-\\d{2}"]} (sut/regex-match? #"\d{4}-\d{2}-\d{2}" nil nil))))
  (testing "match success"
    (is (= {} (sut/regex-match? #"\d{4}-\d{2}-\d{2}" "4444-22-22" nil))))
  (testing "match fail"
    (is (= {:errors ["Value must match the regular expression: \\d{4}-\\d{2}-\\d{2}"]} (sut/regex-match? #"\d{4}-\d{2}-\d{2}" "44442222" nil)))))
