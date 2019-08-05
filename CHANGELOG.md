# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## [Unreleased]
- Validity conditions do not work on multi-valued attributes (#15)

## [1.0.0] — 2019-07-03
- scan-attributes should recursively check attributes (#13)

## [0.2.3] — 2019-06-06
### Changed
- Fix NPE in multi-valued attribute validation (#11)

## [0.2.2] — 2019-04-29
### Added
- Automated release script

## [0.2.1] — 2019-04-26
### Changed
- Fix NPE in validation (#8)
- Handle type mismatches in conditions (#7)

## [0.2.0] — 2019-04-19
### Changed
- Validations can return arbitrary data structures instead of only strings (#1)

## [0.1.0] — 2019-04-09
### Added
- Mechanisms to define a schema via `defschema`
- Functions to access schema attributes and properties
- Mechanisms to define conditions and use them in validations


[0.1.0]: https://github.com/rkaippully/clj-annotations/compare/0.0.0...0.1.0
[0.2.0]: https://github.com/rkaippully/clj-annotations/compare/0.1.0...0.2.0
[0.2.1]: https://github.com/rkaippully/clj-annotations/compare/0.2.0...0.2.1
[0.2.2]: https://github.com/rkaippully/clj-annotations/compare/0.2.1...0.2.2
[0.2.3]: https://github.com/rkaippully/clj-annotations/compare/0.2.2...0.2.3
[1.0.0]: https://github.com/rkaippully/clj-annotations/compare/0.2.3...1.0.0
[Unreleased]: https://github.com/rkaippully/clj-annotations/compare/1.0.0...HEAD
