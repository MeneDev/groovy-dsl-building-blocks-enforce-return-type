# Groovy DSL Building Block: Enforce return type of method.

[![CircleCI](https://circleci.com/gh/MeneDev/groovy-dsl-building-blocks-enforce-return-type/tree/master.svg?style=svg)](https://circleci.com/gh/MeneDev/groovy-dsl-building-blocks-enforce-return-type/tree/master)  [![](https://jitpack.io/v/MeneDev/groovy-dsl-building-blocks-enforce-return-type.svg)](https://jitpack.io/#MeneDev/groovy-dsl-building-blocks-enforce-return-type) [![Twitter URL](https://img.shields.io/twitter/follow/MeneDev.svg?style=social&label=Follow+%20%40MeneDev)](https://twitter.com/MeneDev)

Enforce the return type of a method in groovy. To be used with GroovyShell.

* Optionally allow subtypes and closure coercion
* Optionally allow boxing/unboxing

## Example

Script:
```groovy
def shouldReturnInt() {
  return "a panda"
}
```

Creating the Shell with `EnforceReturnType`:
```groovy
def configuration = new CompilerConfiguration()
def enforceReturnType = new EnforceReturnType(ClassHelper.int_TYPE)
configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
def shell = new GroovyShell(new Binding(), configuration)
```

Result when parsing the Script:
```
org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed:
Script1.groovy: 3: [Static type checking] - Method shouldReturnInt must return int but inferred java.lang.String as return type
 @ line 3, column 18.
             return "a panda"
                    ^
                    
1 error
```

## Usage
### Configuration

```groovy
// Allow subtypes AND coercion
enforceReturnType.allowSubtypes = true

// allow Boxing (int -> Integer) 
enforceReturnType.allowBoxing = true

// allow Unboxing (Integer -> int) 
enforceReturnType.allowUnboxing = true

// limit to non-synthetic MethodNodes named 'int*'
new EnforceReturnType(ClassHelper.int_TYPE,
    { !it.synthetic && it.name.startsWith("int")}
)

```

### Gradle
You can add a dependency to you build via [jitpack](https://jitpack.io/#MeneDev/groovy-dsl-building-blocks-enforce-return-type).
Please do not use the `-SNAPSHOT` version.

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    compile 'com.github.MeneDev:groovy-dsl-building-blocks-enforce-return-type:76c7b7325d'
}
```

For more examples have a look at the [tests](https://github.com/MeneDev/groovy-dsl-building-blocks-enforce-return-type/blob/master/src/test/groovy/de/menedev/groovy/dsl/buildingblocks/EnforceSpec.groovy).
 
## Notes
This was created as an answer to the question [Groovy: How to get statically inferred return type from AST
](https://stackoverflow.com/questions/50337623/groovy-how-to-get-statically-inferred-return-type-from-ast) on Stackoverflow.

