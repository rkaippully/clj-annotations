(ns clj-annotations.conditions-test
  (:require [clojure.test :refer :all]
            [clj-annotations.conditions :as c]))


(deftest and-test
  (letfn [(cond-success [v & ctx]
            {})
          (cond-warning [n]
            (fn [v & ctx]
              {:warnings [(str "warning" n)]}))
          (cond-error [v & ctx]
            {:errors ["error"]})]

    (testing "and - sss"
      (is (= {} ((c/and cond-success cond-success cond-success) (Object.)))))

    (testing "and - ssw"
      (is (= {:warnings ["warning1"]} ((c/and cond-success cond-success (cond-warning 1)) (Object.)))))

    (testing "and - sse"
      (is (= {:errors ["error"]} ((c/and cond-success cond-success cond-error) (Object.)))))

    (testing "and - sws"
      (is (= {:warnings ["warning1"]} ((c/and cond-success (cond-warning 1) cond-success) (Object.)))))

    (testing "and - sww"
      (is (= {:warnings ["warning1"]} ((c/and cond-success (cond-warning 1) (cond-warning 2)) (Object.)))))

    (testing "and - swe"
      (is (= {:warnings ["warning1"]} ((c/and cond-success (cond-warning 1) cond-error) (Object.)))))

    (testing "and - ses"
      (is (= {:errors ["error"]} ((c/and cond-success cond-error cond-success) (Object.)))))

    (testing "and - sew"
      (is (= {:errors ["error"]} ((c/and cond-success cond-error (cond-warning 1)) (Object.)))))

    (testing "and - see"
      (is (= {:errors ["error"]} ((c/and cond-success cond-error cond-error) (Object.)))))

    (testing "and - wss"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) cond-success cond-success) (Object.)))))

    (testing "and - wsw"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) cond-success (cond-warning 2)) (Object.)))))

    (testing "and - wse"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) cond-success cond-error) (Object.)))))

    (testing "and - wws"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) (cond-warning 2) cond-success) (Object.)))))

    (testing "and - www"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) (cond-warning 2) (cond-warning 3)) (Object.)))))

    (testing "and - wwe"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) (cond-warning 2) cond-error) (Object.)))))

    (testing "and - wes"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) cond-error cond-success) (Object.)))))

    (testing "and - wew"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) cond-error (cond-warning 2)) (Object.)))))

    (testing "and - wee"
      (is (= {:warnings ["warning1"]} ((c/and (cond-warning 1) cond-error cond-error) (Object.)))))

    (testing "and - ess"
      (is (= {:errors ["error"]} ((c/and cond-error cond-success cond-success) (Object.)))))

    (testing "and - esw"
      (is (= {:errors ["error"]} ((c/and cond-error cond-success (cond-warning 1)) (Object.)))))

    (testing "and - ese"
      (is (= {:errors ["error"]} ((c/and cond-error cond-success cond-error) (Object.)))))

    (testing "and - ews"
      (is (= {:errors ["error"]} ((c/and cond-error (cond-warning 1) cond-success) (Object.)))))

    (testing "and - eww"
      (is (= {:errors ["error"]} ((c/and cond-error (cond-warning 1) (cond-warning 2)) (Object.)))))

    (testing "and - ewe"
      (is (= {:errors ["error"]} ((c/and cond-error (cond-warning 1) cond-error) (Object.)))))

    (testing "and - ees"
      (is (= {:errors ["error"]} ((c/and cond-error cond-error cond-success) (Object.)))))

    (testing "and - eew"
      (is (= {:errors ["error"]} ((c/and cond-error cond-error (cond-warning 1)) (Object.)))))

    (testing "and - eee"
      (is (= {:errors ["error"]} ((c/and cond-error cond-error cond-error) (Object.)))))))

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
      (is (= {} ((c/or cond-success cond-success cond-success) (Object.)))))

    (testing "or - ssw"
      (is (= {} ((c/or cond-success cond-success (cond-warning 1)) (Object.)))))

    (testing "or - sse"
      (is (= {} ((c/or cond-success cond-success (cond-error 1)) (Object.)))))

    (testing "or - sws"
      (is (= {} ((c/or cond-success (cond-warning 1) cond-success) (Object.)))))

    (testing "or - sww"
      (is (= {} ((c/or cond-success (cond-warning 1) (cond-warning 2)) (Object.)))))

    (testing "or - swe"
      (is (= {} ((c/or cond-success (cond-warning 1) (cond-error 1)) (Object.)))))

    (testing "or - ses"
      (is (= {} ((c/or cond-success (cond-error 1) cond-success) (Object.)))))

    (testing "or - sew"
      (is (= {} ((c/or cond-success (cond-error 1) (cond-warning 1)) (Object.)))))

    (testing "or - see"
      (is (= {} ((c/or cond-success (cond-error 1) (cond-error 2)) (Object.)))))

    (testing "or - wss"
      (is (= {} ((c/or (cond-warning 1) cond-success cond-success) (Object.)))))

    (testing "or - wsw"
      (is (= {} ((c/or (cond-warning 1) cond-success (cond-warning 2)) (Object.)))))

    (testing "or - wse"
      (is (= {} ((c/or (cond-warning 1) cond-success (cond-error 1)) (Object.)))))

    (testing "or - wws"
      (is (= {} ((c/or (cond-warning 1) (cond-warning 2) cond-success) (Object.)))))

    (testing "or - www"
      (is (= {:warnings ["warning3"]} ((c/or (cond-warning 1) (cond-warning 2) (cond-warning 3)) (Object.)))))

    (testing "or - wwe"
      (is (= {:errors ["error1"]} ((c/or (cond-warning 1) (cond-warning 2) (cond-error 1)) (Object.)))))

    (testing "or - wes"
      (is (= {} ((c/or (cond-warning 1) (cond-error 1) cond-success) (Object.)))))

    (testing "or - wew"
      (is (= {:warnings ["warning2"]} ((c/or (cond-warning 1) (cond-error 1) (cond-warning 2)) (Object.)))))

    (testing "or - wee"
      (is (= {:errors ["error2"]} ((c/or (cond-warning 1) (cond-error 1) (cond-error 2)) (Object.)))))

    (testing "or - ess"
      (is (= {} ((c/or (cond-error 1) cond-success cond-success) (Object.)))))

    (testing "or - esw"
      (is (= {} ((c/or (cond-error 1) cond-success (cond-warning 1)) (Object.)))))

    (testing "or - ese"
      (is (= {} ((c/or (cond-error 1) cond-success (cond-error 2)) (Object.)))))

    (testing "or - ews"
      (is (= {} ((c/or (cond-error 1) (cond-warning 1) cond-success) (Object.)))))

    (testing "or - eww"
      (is (= {:warnings ["warning2"]} ((c/or (cond-error 1) (cond-warning 1) (cond-warning 2)) (Object.)))))

    (testing "or - ewe"
      (is (= {:errors ["error2"]} ((c/or (cond-error 1) (cond-warning 1) (cond-error 2)) (Object.)))))

    (testing "or - ees"
      (is (= {} ((c/or (cond-error 1) (cond-error 2) cond-success) (Object.)))))

    (testing "or - eew"
      (is (= {:warnings ["warning1"]} ((c/or (cond-error 1) (cond-error 2) (cond-warning 1)) (Object.)))))

    (testing "or - eee"
      (is (= {:errors ["error3"]} ((c/or (cond-error 1) (cond-error 2) (cond-error 3)) (Object.)))))))
