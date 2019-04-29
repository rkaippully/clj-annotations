[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](LICENSE)
[![Build Status](https://travis-ci.org/rkaippully/clj-annotations.svg?branch=master)](https://travis-ci.org/rkaippully/clj-annotations)
[![codecov](https://codecov.io/gh/rkaippully/clj-annotations/branch/master/graph/badge.svg)](https://codecov.io/gh/rkaippully/clj-annotations)
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.rkaippully/clj-annotations.svg)](https://clojars.org/org.clojars.rkaippully/clj-annotations)

```clj
[org.clojars.rkaippully/clj-annotations "0.2.2"]
```

## Motivation
Clojure is very versatile in manipulating complex data structures. This ease of use comes
with a cost though. It is not easy to understand the structure of the data because of the
dynamic nature of clojure. [There](https://github.com/clojure/core.typed)
[have](https://github.com/plumatic/schema) [been](https://clojure.org/guides/spec) various
attempts to solve this problem, each with its pros and cons. A common pattern in all these
solutions is to declare the structure or shape of the data and validate values against
it.

But what if we could extend this solution for aspects other than data validation? For
example, one could declaratively specify whether a partcular piece of data should be
skipped from logging. This will help prevent leaking of sensitive data. Or one could
specify whether a string value is supposed to be used in a case sensitive manner. This
will be useful in search operations.

`clj-annotations` provides mechanisms to define schemas which allow one to attach
arbitrary properties to attributes of the schema. These properties can then be searched,
retrieved and used in various contexts.

## Schemas, Attributes, and Properties
The first step in using `clj-annotations` is to define a schema using the `defschema`
macro. For example, here is a schema defining a data structure describing a person.

``` clj
user=> (require ['clj-annotations.core :as 'ann :refer ['defschema]])

user=> (defn non-blank
         [s _]
         (if (clojure.string/blank? s)
           {:errors ["Expected a non-blank value"]}
           {}))

user=> (defschema person
         :name        "Person"
         :description "Represents a person in the system"
         :attributes
         {:id
          {:type     :number
           :required true
           :label    "Identifier"}

          :email
          {:type     :string
           :required true
           :label    "Email Address"
           :validity non-blank}})
```

This defines a schema for `person` with a name and description. It has two attributes -
`:id` and `:email`. These attributes have some properties associated with them - `:type`,
`:required`, and `:label`. The `:email` attribute has an additional property named
`:validity`.

The properties can be any key-value pairs. They do not have any inherent semantics imposed
by `defschema`. These properties can be used in different ways depending on the use
case. For example, a validation library based on this schema may use `:type`, `:required`,
and `:validity` properties to check if a value conforms to the schema. A UI library may
use the `:label` property to display these attributes in the UI.

There are two ways to access information from the schema. The first is to use
`get-annotations`. This is a swiss army knife to extract, the entire schema, properties of
an attribute, value of a specific property with an optional "not found" override.

``` clj
user=> (ann/get-annotations person)
{:id {:type :number, :required true, :label "Identifier"}, :email {:type :string, :required true, :label "Email Address", :validity #object[user$non_blank 0x457d169a "user$non_blank@457d169a"]}}
user=> (ann/get-annotations person :id)
{:type :number, :required true, :label "Identifier"}
user=> (ann/get-annotations person :id :label)
"Identifier"
user=> (ann/get-annotations person :id :nullable?)
nil
user=> (ann/get-annotations person :id :nullable? :not-found)
:not-found
```

Another use case is to find all attributes from a schema having a specific value for a
property. For example, how do we find all attributes with `:type` property set to
`:string`? Its possible to do that with `scan-attributes` function.

``` clj
user=> (ann/scan-attributes person :type :string)
(:email)
```

## Schema Composition
In many practical scenarios, you might want to build schemas based on composable
parts. For example, one might define a person schema as above with id and name
attributes and then might want to use them in employee and customer schemas. This can
easily be done with the `:include` argument to `defschema`.

``` clj
user=> (defn company-domain
         [s _]
         (if (clojure.string/ends-with? s "@example.com")
           {}
           {:errors ["Email must have example.com domain"]}))

user=> (defschema employee
         :include person
         :attributes
         {:email
          {:validity company-domain}

          :employeeId
          {:type     :number
           :required true}})
```

The employee schema has all the attributes and properties from person schema except that
the `:validity` property of `:email` attribute is overridden. It also has an additional
attribute `:employeeId`.

The `:include` argument could point to a single schema or a sequence of schemas. In the
latter case, the schema attributes will be merged from left to right. In case of a
conflict in any properties, the last one in the sequence will win.

## Validation
`clj-annotations` includes a validation framework based on the schema definitions. Let us
get started with an example:

``` clj
(require ['clj-annotations.validation :as 'v])

user=> (v/validate-object employee {})
({:kind :missing-required-attribute, :message "Missing required attribute", :path "/email", :level :error} {:kind :missing-required-attribute, :message "Missing required attribute", :path "/employeeId", :level :error} {:kind :missing-required-attribute, :message "Missing required attribute", :path "/id", :level :error})

user=> (v/validate-object employee {:email "test@google.com" :employeeId true :id "foo"})
({:kind :validation-failure, :message "Email must have example.com domain", :path "/email", :level :error} {:kind :type-mismatch, :message "Expected a number but found boolean", :path "/employeeId", :level :error} {:kind :type-mismatch, :message "Expected a number but found string", :path "/id", :level :error})

user=> (v/validate-object employee {:email "test@example.com" :employeeId 10042 :id 42})
()
```

The validation uses the `:type`, `:required`, and `:validity` properties of the attributes
to check whether an object conforms to the schema.

The validation logic is highly customizable. It accepts an options map with the below
mentioned keys to tune the validation behavior.

  - `:make-result` - a function that takes the schema, the attribute value, the attribute
  path, and a validation result and returns a validation result as a vector. See
  `make-validation-result` for an example.
  - `:type-checks` - a map from type names to functions validating the conformance of a
  value to those types. See `type-checks` for an example.
  - `:fail-on-unsupported-attributes?` - if true an error will be reported if the `obj`
  contains an attribute that is not defined in `schema`

## License

Copyright © 2019 Raghu Kaippully <rkaippully@gmail.com> 

Distributed under the Mozilla Public License version 2.0.
