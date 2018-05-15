# Groovy Get-Source

[![CircleCI](https://circleci.com/gh/MeneDev/groovy-dsl-building-blocks-enforce-return-type/tree/master.svg?style=svg)](https://circleci.com/gh/MeneDev/groovy-get-source/tree/master)

Enforce the return type of a method in groovy. To be used with GroovyShell.
## Example

Creating the Shell:
```groovy
def configuration = new CompilerConfiguration()
def enforceReturnType = new EnforceReturnType(ClassHelper.int_TYPE)
configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
def shell = new GroovyShell(new Binding(), configuration)
```

Script:
```groovy
def shouldReturnInt() {
  return "a panda"
}
```

## Usage
You can add a dependency to you build via jitpack.

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.MeneDev:groovy-dsl-building-blocks-enforce-return-type:-SNAPSHOT'
}
```

For more examples have a look at the tests.
 
## Notes
This was created as an answer to the question [Groovy: How to get statically inferred return type from AST
](https://stackoverflow.com/questions/50337623/groovy-how-to-get-statically-inferred-return-type-from-ast) on Stackoverflow.