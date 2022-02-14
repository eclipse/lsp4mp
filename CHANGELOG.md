# MicroProfile Language Server Changelog

## [0.4.0](https://github.com/eclipse/lsp4mp/milestone/4?closed=1) (March 16, 2022)

### Enhancements
 * Support validation and code actions for `@ConfigProperty`. See [#90](https://github.com/eclipse/lsp4mp/issues/90), [#176](https://github.com/eclipse/lsp4mp/issues/176), [#147](https://github.com/eclipse/lsp4mp/issues/147).
 * Completion for properties defined using `@ConfigProperties`. See [#80](https://github.com/eclipse/lsp4mp/issues/80).
 * Support validation for `@Retry` annotation and its member values. See [#191](https://github.com/eclipse/lsp4mp/pull/191), [#196](https://github.com/eclipse/lsp4mp/issues/196).
 * Diagnostics for `@Asynchronous`, `@Bulkhead` & `@Timeout` annotations. See [#74](https://github.com/eclipse/lsp4mp/issues/74), [#184](https://github.com/eclipse/lsp4mp/pull/184), [#185](https://github.com/eclipse/lsp4mp/pull/185).
 * Support the `@ApplicationPath` annotation to handle the project URL. See [#179](https://github.com/eclipse/lsp4mp/issues/179).
 * Diagnostics for invalid annotation parameter values. See [#77](https://github.com/eclipse/lsp4mp/issues/77).
 * Reference only property declared in properties file in property expression. See [#205](https://github.com/eclipse/lsp4mp/issues/205).
 * Support for default value inside properties expression. See [#201](https://github.com/eclipse/lsp4mp/issues/201).
 * Exclude unassigned property with code action. See [#187](https://github.com/eclipse/lsp4mp/pull/187).
 * Binary dynamic properties should be generated after an update. See [#159](https://github.com/eclipse/lsp4mp/pull/159).
 * Support for config profiles. See [#78](https://github.com/eclipse/lsp4mp/issues/78).

### Bug Fixes
 * Provide API to configure root path of JAX RS resources. See [#174](https://github.com/eclipse/lsp4mp/pull/174).
 * projectLabels command doesn't support `jdt://` URIs. See [#220](https://github.com/eclipse/lsp4mp/issues/220).
 * Fix bug with missing definition hover for multiple annotation members. See [#216](https://github.com/eclipse/lsp4mp/pull/216).
 * Support optional property reference hover for annotation members. See [#211](https://github.com/eclipse/lsp4mp/pull/211).
 * Do not rebuild list of configuration properties when MicroProfile config sources are updated in the build directory. See [#162](https://github.com/eclipse/lsp4mp/issues/162).
 * Deadlock when client is sending burst of request. See [#177](https://github.com/eclipse/lsp4mp/issues/177).
 * Exclude the method that's being annotated when showing completion for fallback method. See [#148](https://github.com/eclipse/lsp4mp/issues/148).
 * SingleMemberAnnotation diagnostics not supported by annotationValidator. See [#188](https://github.com/eclipse/lsp4mp/issues/188).
 * Add 'shouldLanguageServerExitOnShutdown' to ExtendedClientCapabilities. See [#172](https://github.com/eclipse/lsp4mp/pull/172).
 * Fix class cast exception. See [#182](https://github.com/eclipse/lsp4mp/pull/182).

### Build
 * Update o.e.jdt.ls.tp dependency to 1.7.0 Release. See [#217](https://github.com/eclipse/lsp4mp/pull/217).
 * Update eclipse-jarsigner-plugin. See [#157](https://github.com/eclipse/lsp4mp/pull/157).

### Other
 * Move `@ApplicationPath` SearchEngine to JaxRsContext. See [#208](https://github.com/eclipse/lsp4mp/issues/208).
 * MicroProfileMetricsDiagnosticsParticipant difficult to read. See [#199](https://github.com/eclipse/lsp4mp/issues/199).
 * Remove QuarkusConfigSourceProvider. See [#160](https://github.com/eclipse/lsp4mp/pull/160).
 * Extension point for contributing configuration sources. See [#149](https://github.com/eclipse/lsp4mp/pull/149).

## [0.3.0](https://github.com/eclipse/lsp4mp/milestone/3?closed=1) (July 15, 2021)

### Enhancements

 * Completion for `fallbackMethod` in `@Fallback` annotation. See [#34](https://github.com/eclipse/lsp4mp/issues/34).

### Other

 * Remove pack200 support. See [#134](https://github.com/eclipse/lsp4mp/pull/134).

## [0.2.0](https://github.com/eclipse/lsp4mp/milestone/2?closed=1) (March 31, 2021)

### Enhancements

 * Support arbitrary number of member values in `PropertiesHoverParticipant`. See [#124](https://github.com/eclipse/lsp4mp/pull/124).
 * Add extension point to contribute properties to exclude from validation. See [#95](https://github.com/eclipse/lsp4mp/issues/95).
 * Definition support from Java to properties for `ConfigProperty/name`. See [#88](https://github.com/eclipse/lsp4mp/issues/88).
 * Automatically infer package names when inserting class snippets. See [#60](https://github.com/eclipse/lsp4mp/issues/60).
 * Support `handle-as` for metadata properties. See [#39](https://github.com/eclipse/lsp4mp/issues/39).
 * Display the different values for the different profiles in Java `@ConfigProperty` Hover. See [#98](https://github.com/eclipse/lsp4mp/issues/98).

### Bug Fixes

 * Trailing tab causes infinite loop in parser. See [#112](https://github.com/eclipse/lsp4mp/issues/112).
 * Prevent NPEs when working with MP 4.0 features. See [#119](https://github.com/eclipse/lsp4mp/issues/119).
 * Enhance the error message when out of bounds is detected. See [#114](https://github.com/eclipse/lsp4mp/pull/114).
 * Use `kill -0` instead of `ps -p` in `ParentProcessWatcher`. See [#110](https://github.com/eclipse/lsp4mp/issues/110).
 * Move quarkus test project names to quarkus-ls repo. See [#7](https://github.com/eclipse/lsp4mp/issues/7).
 * Wrong/Missing Log Levels in property files. See [#15](https://github.com/eclipse/lsp4mp/pull/105).
 * `mp.messaging` properties now work for Emitters. See [#127](https://github.com/eclipse/lsp4mp/pull/127).

### Other

 * Move `MicroProfileForJavaAssert` into `internal.core` class. See [#125](https://github.com/eclipse/lsp4mp/pull/125).
 * Sequence diagram of properties completion. See [#75](https://github.com/eclipse/lsp4mp/issues/75).
 * Fix Tools for MP name in sequence diagrams. See [#93](https://github.com/eclipse/lsp4mp/pull/93).

## [0.1.0](https://github.com/eclipse/lsp4mp/milestone/1?closed=1) (September 21, 2020)

### Enhancements

 * Java snippets for `microprofile rest client`. See [#55](https://github.com/eclipse/lsp4mp/issues/55).
 * Filter out duplicate completion items coming from MP implementations. See [#48](https://github.com/eclipse/lsp4mp/issues/48).
 * CDI scope diagnostics for `mp metrics @Gauge`. See [#46](https://github.com/eclipse/lsp4mp/issues/46).
 * Highlight support for property expression. See [#40](https://github.com/eclipse/lsp4mp/issues/40).
 * Diagnostics for ` mp-fault-tolerance fallbackMethod` . See [#33](https://github.com/eclipse/lsp4mp/issues/33).
 * Java `snippets for jax-rs`. See [#31](https://github.com/eclipse/lsp4mp/issues/31).
 * Snippets for new `microprofile health liveness / readiness checks`. See [#28](https://github.com/eclipse/lsp4mp/issues/28).
 * Properties support for `microprofile-graphql`. See [#27](https://github.com/eclipse/lsp4mp/issues/27).
 * Properties support for `microprofile-reactive-messaging`. See [#26](https://github.com/eclipse/lsp4mp/issues/26).
 * Hover for Property Expressions. See [#24](https://github.com/eclipse/lsp4mp/issues/24).
 * Properties support for microprofile-jwt-auth. See [#23](https://github.com/eclipse/lsp4mp/issues/23).
 * Property expression validation. See [#21](https://github.com/eclipse/lsp4mp/pull/21).
 * Property expression definition. See [#19](https://github.com/eclipse/lsp4mp/pull/19).
 * Hardcoded support for boolean converter. See [#17](https://github.com/eclipse/lsp4mp/pull/17).
 * Properties support for `microprofile-health`. See [#16](https://github.com/eclipse/lsp4mp/issues/16).
 * Model and completion for property expressions. See [#13](https://github.com/eclipse/lsp4mp/pull/13).

### Bug Fixes

 * NullPointerException with symbols. See [#66](https://github.com/eclipse/lsp4mp/issues/66).
 * Fix duplicate of `quarkus-properties` when registering `textDocument/rangeFormatting`. See [#52](https://github.com/eclipse/lsp4mp/pull/52).
 * Rename settings prefix to microprofile. See [#51](https://github.com/eclipse/lsp4mp/pull/51).
 * Fix missing unit in Gauge metrics snippet. See [#47](https://github.com/eclipse/lsp4mp/pull/47).
 * Escape special characters within LSP snippets. See [#29](https://github.com/eclipse/lsp4mp/pull/29).
 * Completion in properties file gives enum values before `=`. See [#14](https://github.com/eclipse/lsp4mp/issues/14).
 * Remove references to quarkusLanguageServer. See [#11](https://github.com/eclipse/lsp4mp/pull/11).
 * Rename settings prefix to microprofile. See [#4](https://github.com/eclipse/lsp4mp/pull/4).

### Build

 * Require Java 11 to build/run. See [#12](https://github.com/eclipse/lsp4mp/issues/12).
 * Collect all JUnit reports in Jenkins. See [#3](https://github.com/eclipse/lsp4mp/pull/3).

### Other

 * Add gitter link to readme. See [#76](https://github.com/eclipse/lsp4mp/pull/76).
 * List vscode-microprofile under clients. See [#72](https://github.com/eclipse/lsp4mp/pull/72).
 * Improve architecture documentation. See [#68](https://github.com/eclipse/lsp4mp/issues/68).
 * Update file headers to use EPL 2.0. See [#53](https://github.com/eclipse/lsp4mp/issues/53).
 * Add CONTRIBUTING.md. See [#49](https://github.com/eclipse/lsp4mp/pull/49).
 * Add diagram to the README describing MicroProfileLS and MicroProfile JDT. See [#35](https://github.com/eclipse/lsp4mp/pull/35).
 * Add diagram in README. See [#9](https://github.com/eclipse/lsp4mp/issues/9).
 * Update README. See [#6](https://github.com/eclipse/lsp4mp/pull/6).
 * Initial code contribution. See [#1](https://github.com/eclipse/lsp4mp/pull/1).
