# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).


## [Unreleased]

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
[Unreleased]: https://github.com/rkaippully/clj-annotations/compare/0.2.0...HEAD
