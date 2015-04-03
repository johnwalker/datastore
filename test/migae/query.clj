(ns migae.query
  (:refer-clojure :exclude [name hash])
  (:import [com.google.appengine.tools.development.testing
            LocalServiceTestHelper
            LocalServiceTestConfig
            LocalMemcacheServiceTestConfig
            LocalMemcacheServiceTestConfig$SizeUnit
            LocalMailServiceTestConfig
            LocalDatastoreServiceTestConfig
            LocalUserServiceTestConfig]
           [com.google.appengine.api.datastore
            EntityNotFoundException]
           [com.google.apphosting.api ApiProxy])
  ;; (:use [clj-logging-config.log4j])
  (:require [clojure.test :refer :all]
            [migae.infix :as infix]
            [migae.datastore :as ds]
            [clojure.tools.logging :as log :only [trace debug info]]))
;            [ring-zombie.core :as zombie]))

(defmacro should-fail [body]
  `(let [report-type# (atom nil)]
     (binding [clojure.test/report #(reset! report-type# (:type %))]
       ~body)
     (testing "should fail"
       (is (= @report-type# :fail )))))

;  (:require [migae.migae-datastore.EntityMap])
  ;; (:use clojure.test
  ;;       [migae.migae-datastore :as ds]))

;; (defn datastore [& {:keys [storage? store-delay-ms
;;                            max-txn-lifetime-ms max-query-lifetime-ms
;;                            backing-store-location]
;;                     :or {storage? false}}]
;;   (let [ldstc (LocalDatastoreServiceTestConfig.)]
;;     (.setNoStorage ldstc (not storage?))
;;     (when-not (nil? store-delay-ms)
;;       (.setStoreDelayMs ldstc store-delay-ms))
;;     (when-not (nil? max-txn-lifetime-ms)
;;       (.setMaxTxnLifetimeMs ldstc max-txn-lifetime-ms))
;;     (when-not (nil? max-query-lifetime-ms)
;;       (.setMaxQueryLifetimeMs ldstc max-query-lifetime-ms))
;;     (if-not (nil? backing-store-location)
;;         (.setBackingStoreLocation ldstc backing-store-location)
;;         (.setBackingStoreLocation ldstc "/dev/null"))
;;         ;; (.setBackingStoreLocation ldstc (if (= :windows (os-type))
;;         ;;                                     "NUL"
;;         ;;                                     "/dev/null")))
;;     ldstc))

;; (defn- make-local-services-fixture-fn [services hook-helper]
(defn- ds-fixture
  [test-fn]
  (let [;; environment (ApiProxy/getCurrentEnvironment)
        ;; delegate (ApiProxy/getDelegate)
        helper (LocalServiceTestHelper.
                (into-array LocalServiceTestConfig
                            [(LocalDatastoreServiceTestConfig.)]))]
    (do
        (.setUp helper)
        (ds/init)
        (test-fn)
        (.tearDown helper))))
        ;; (ApiProxy/setEnvironmentForCurrentThread environment)
        ;; (ApiProxy/setDelegate delegate))))

;(use-fixtures :once (fn [test-fn] (dss/get-datastore-service) (test-fn)))
(use-fixtures :each ds-fixture)

(deftest ^:query q1
  (testing "ds query"
    (let [em1 (ds/emap!! [:Foo/Bar] {:a 1})
          em2 (ds/emap!! [:Foo/Bar] {:a 2})
          em3 (ds/emap!! [:Foo/Bar] {:a 3})]

      )))

(deftest ^:query kindless
  (testing "ds kindless queries"
    (ds/emaps!! [:A] [{:a 1} {:a 2} {:a 3}])
    (ds/emap!!  [:A/A] {:a 1})
    (ds/emaps!! [:A/A :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/A :B] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/A :C] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:B] [{:a 1} {:a 2} {:a 3}])
    (ds/emap!!  [:A/B] {:a 1})
    (ds/emaps!! [:A/B :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/B :B] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/B :C] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:C] [{:a 1} {:a 2} {:a 3}])
    (ds/emap!!  [:A/C] {:a 1})
    (ds/emaps!! [:A/C :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/C :B] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/C :C] [{:a 1} {:a 2} {:a 3}])

    (let [ems (ds/emaps?? [])]
      ;; ems: all entities (6)
      (log/trace "ems" ems)
      (doseq [em ems] (log/trace (meta em) em)))

    ;; (let [ems (ds/emaps?? [] {:migae/gt [:A/B :B/A]})]
    (let [ems (ds/emaps?? [] '(> [:A/B :B/A]))]
      (log/trace "ems" ems)
      (doseq [em ems] (log/trace (meta em) em)))

    (let [ems (ds/emaps?? [] '(< [:A/B :B/d1 :C/d2]))]
      (log/trace "ems" ems)
      (doseq [em ems] (log/trace (meta em) em)))

    ;; ;; illegal: no filters allowed on kindless queries
    ;; (let [ems2 (ds/emaps?? [] {:a 2})]
    ;;   )
    ))

(deftest ^:query by-kind
  (testing "ds query by kind"
    (ds/emaps!! [:A] [{:a 1} {:a 2} {:a 3}])
    (ds/emap!!  [:A/A] {:a 1})
    (ds/emaps!! [:A/A :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/A :B] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/A :C] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:B] [{:a 1} {:a 2} {:a 3}])
    (ds/emap!!  [:A/B] {:a 1})
    (ds/emaps!! [:A/B :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/B :B] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/B :C] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:C] [{:a 1} {:a 2} {:a 3}])
    (ds/emap!!  [:A/C] {:a 1})
    (ds/emaps!! [:A/C :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/C :B] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/C :C] [{:a 1} {:a 2} {:a 3}])

    (let [ems1 (ds/emaps?? [:A])
          ems2 (ds/emaps?? [:A] {:a 2})]
      (log/trace "ems1" ems1)
      (log/trace "ems2" ems2)
      )

    (let [ems1 (ds/emaps?? [:A/B :A] {:a 2})
          ems2 (ds/emaps?? [:A/B :B] {:a 2})]
          )
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest ^:query by-ancestor
  (testing "query by ancestor"
    (ds/emap!! [:A/B]{})
    (ds/emaps!! [:A/B :C] [{:a 1} {:a 2} {:a 3}])
    (let [parent (try (ds/emaps?? [:A/B]{})
                      (catch EntityNotFoundException e
                        (log/trace (.getMessage e))
                        (throw e)))
          childs (try (ds/emaps?? [:A/B :C])
                      (catch EntityNotFoundException e
                        (log/trace (.getMessage e))
                        nil))
          k (key parent) ;; k is a keychain (vector) of keylinks (keywords)
          foo (log/trace "k:" k (type k) (class k))
          children (try (ds/emaps?? (merge k :C)) ; <- merge keylinks into [] keychain
                      (catch EntityNotFoundException e
                        (log/trace (.getMessage e))
                        nil))
          ]
      (log/trace "parent" (ds/epr parent))
      (log/trace "childs")
      (doseq [child childs] (log/trace "child" (ds/epr child)))
      (log/trace "children")
      (doseq [child children] (log/trace "child" (ds/epr child)))
      )))

(deftest ^:query by-property
  (testing "ds property filter query"
    (ds/emaps!! [:A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/B :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:X/Y :A] [{:a 1} {:a 2} {:a 3}])
    (ds/emaps!! [:A/C :A] [{:a 1} {:a 2} {:a 3}])
    (let [ems (ds/emaps?? [:A] {:a '(= 2)})]
      (log/trace "ems:")
      (doseq [em ems]
        (log/trace (ds/epr em))))

    (let [ems (ds/emaps?? [:A] {:a '(>= 2)})]
      (log/trace "ems:")
      (doseq [em ems]
        (log/trace (ds/epr em))))

    (let [ems (ds/emaps?? [:A] {:a '(> 2)})]
      (log/trace "ems:")
      (doseq [em ems]
        (log/trace (ds/epr em))))
    ))

(deftest ^:query by-key
  (testing "entitymap get by key")
  (let [putit (ds/emap!! [:Species/Felis_catus] {})
        getit (ds/emap?? [:Species/Felis_catus])
        getit2 (ds/emap?? :Species/Felis_catus)
        getit3 (ds/emap?? (keyword "Species" "Felis_catus"))]
    ;; (log/trace "putit " putit)
    ;; (log/trace "key putit " (ds/key putit))
    ;; (log/trace "type putit " (type putit))
    ;; (log/trace "getit " getit)
    ;; (log/trace "getit2 " getit2)
    ;; (log/trace "getit3 " getit3)
    (is (= (ds/key putit) (ds/key getit) (ds/key getit2) (ds/key getit3)))
    (is (= (type putit) migae.datastore.EntityMap))
    (is (= (try (ds/emap?? (keyword "Group" "foo"))
                      (catch EntityNotFoundException e EntityNotFoundException))
            EntityNotFoundException))
    ))

(deftest ^:query kind-query
  (testing "kind query"
    (let [e1 (ds/emaps?? :Foo)]
      (log/trace e1))))

;; ################################################################
(deftest ^:query emaps-q
  (testing "emaps?? 1"
    (let [em1 (ds/emap!! [:Group] {:name "Acme"})
          em2 (ds/emap!! [:Group] (fn [e] (assoc e :name "Tekstra")))
          ems (ds/emaps?? :Group)]
      (log/trace "ems " ems)
      (log/trace "ems type " (type ems))
      (log/trace (format "(seq? ems) %s\n" (seq? ems)))
      [(doseq [e ems]
         (do
           (log/trace (format "e: %s" e))
           (log/trace "(meta e): " (meta e))
           (log/trace "(type e): " (type e))
           (log/trace "(.entity e): " (.entity e))
           {:id (ds/id e) :name (e :name)}))]
      )))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(deftest ^:query ancestor-path
  (testing "emaps?? ancestor query"
    (let [e1 (ds/emap!! [:Family/Felidae :Subfamily/Felinae :Genus/Felis :Species/Felis_catus]{})
          f1 (ds/emap?? [:Family/Felidae :Subfamily/Felinae :Genus/Felis :Species/Felis_catus])
          e2 (ds/emap!! [:Species/Felis_catus]{})
          f2 (ds/emap?? [:Species/Felis_catus])]
      (log/trace "e1 " e1)
      (log/trace "f1 " f1)
      (log/trace "e2 " e2)
      (log/trace "f2 " f2))))

(deftest ^:query ancestor-query
  (testing "emaps?? ancestor query"
    (let [acme (ds/emap!! [:Group] {:name "Acme"})
          k (ds/key acme)
          foo (log/trace "key: " k)
          id (ds/id k)
          joe (ds/emap!! [k :Member/Joe] {:fname "Joe" :lname "Cool"})
          ;; joeq  (ds/emaps?? [:Group/Acme :Member/Joe])
          ;; joev  (ds/emaps?? :Member/Joe)
          jane (ds/emap!! [k :Member/Jane] {:fname "Jane" :lname "Hip"})
          frank (ds/emap!! [:Member/Frank] {:fname "Frank" :lname "Funk"})
          root (ds/emaps?? k)
          members (ds/emaps?? :Member)
          membersV (ds/emaps?? [:Member])
          members-acme (ds/emaps?? [k :Member])
          ] ; ancestor query
      ;; (log/trace "root: " root)
      ;; (log/trace "all members: " members)
      ;; (log/trace "all membersV: " membersV)
      (log/trace "acme members: " members-acme)
      (log/trace "joe " joe)
      ;; (log/trace "joeq " joeq)
      ;; (log/trace "joev " joev)
      ;; (is (=  (ds/emaps?? :Group/Acme)  (ds/emaps?? [:Group/Acme])))
      ;; (is (ds/key=  (first (ds/emaps?? [:Group/Acme :Member/Joe])) joe))
      (is (=  (count (ds/emaps?? :Member)) 3))
      (is (=  (count (ds/emaps?? [:Member])) 3))
      (is (=  (count (ds/emaps?? [k :Member])) 2))
      )))

;; ################################################################

;; (deftest ^:query query-entities-1
;;   (testing "query 1"
;;     (let [q (dsqry/entities)]
;;       (log/trace "query entities 1" q)
;;       )))

;; (deftest ^:query query-entities-2
;;   (testing "query 2"
;;     (let [q (dsqry/entities :kind :Employee)]
;;       (log/trace "query entities 2" q)
;;       )))


;; ;; (deftest ^:query query-ancestors-1
;; ;;   (testing "query ancestors-1"
;; ;;     (let [k (dskey/make "foo" "bar")
;; ;;           q (dsqry/ancestors :key k)]
;; ;;       (log/trace "query ancestors-1:" q)
;; ;;       )))

;; (deftest ^:query query-ancestors-2
;;   (testing "query ancestors-2"
;;     (let [q (dsqry/ancestors :kind "foo" :name "bar")]
;;       (log/trace "query ancestors-2:" q)
;;       )))

;; (deftest ^:query query-ancestors-3
;;   (testing "query ancestors-3"
;;     (let [q (dsqry/ancestors :kind "foo" :id 99)]
;;       (log/trace "query ancestors-3:" q)
;;       )))

;; (deftest ^:query query-ancestors-3
;;   (testing "query ancestors-3"
;;     (let [q (dsqry/ancestors :kind "foo" :id 99)]
;;       (log/trace "query ancestors-3:" q)
;;       )))

;; (deftest ^:query query-ancestors-4
;;   (testing "query ancestors-4"
;;     (let [q (dsqry/ancestors :kind :Person :id 99)]
;;       (log/trace "query ancestors-3:" q)
;;       )))
