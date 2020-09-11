# MicroProfile Language Server Changelog

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