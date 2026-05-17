package kanton.core.cli.models

data class DepEntry(val coord: String, val imports: List<String>)

data class OptionConfig(val names: List<String>, val help: String, val typeFunc: String?, val default: String?)

data class ArgumentConfig(val name: String, val help: String, val typeFunc: String?, val default: String?)

data class InjectionContext(val prefix: String, val suffix: String, val className: String)
