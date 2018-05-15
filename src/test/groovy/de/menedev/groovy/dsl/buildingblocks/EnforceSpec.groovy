package de.menedev.groovy.dsl.buildingblocks

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import spock.lang.Specification
import spock.lang.Unroll

class EnforceSpec extends Specification {
    @Unroll
    def "enforcing #enforcedType produces no error on a method explicitly returning #returnedValue"() {
        given:
        def script = """
        def returnsThingsImplicitly() {
            return $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        noExceptionThrown()

        where:
        enforcedType             | returnedValue
        ClassHelper.Integer_TYPE | "1 as Integer"
        ClassHelper.Integer_TYPE | "2 as Integer"
        ClassHelper.Integer_TYPE | "1 + 1 as Integer"
        ClassHelper.int_TYPE     | "1"
        ClassHelper.int_TYPE     | "2"
        ClassHelper.int_TYPE     | "1 + 1"
        ClassHelper.boolean_TYPE | "true"
        ClassHelper.boolean_TYPE | "false"
        ClassHelper.boolean_TYPE | "1 < 2"
        ClassHelper.Boolean_TYPE | "true as Boolean"
        ClassHelper.Boolean_TYPE | "false as Boolean"
        ClassHelper.Boolean_TYPE | "(1 < 2) as Boolean"
    }

    @Unroll
    def "enforcing #enforcedType produces no error on a method implicitly returning #returnedValue"() {
        given:
        def script = """
        def returnsThingsImplicitly() {
            $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        noExceptionThrown()

        where:
        enforcedType             | returnedValue
        ClassHelper.Integer_TYPE | "1 as Integer"
        ClassHelper.Integer_TYPE | "2 as Integer"
        ClassHelper.Integer_TYPE | "1 + 1 as Integer"
        ClassHelper.int_TYPE     | "1"
        ClassHelper.int_TYPE     | "2"
        ClassHelper.int_TYPE     | "1 + 1"
        ClassHelper.boolean_TYPE | "true"
        ClassHelper.boolean_TYPE | "false"
        ClassHelper.boolean_TYPE | "1 < 2"
        ClassHelper.Boolean_TYPE | "true as Boolean"
        ClassHelper.Boolean_TYPE | "false as Boolean"
        ClassHelper.Boolean_TYPE | "(1 < 2) as Boolean"
    }

    @Unroll
    def "can return #returnedValue when enforcing #enforcedType and #allowOrNot boxing"() {
        given:
        def script = """
        def returnsThingsImplicitly() {
            $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)

        and:
        enforceReturnType.allowSubtypes = true
        enforceReturnType.allowBoxing = allowOrNot == "allow"

        and:
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        noExceptionThrown()

        where:
        enforcedType             | returnedValue  | allowOrNot
        ClassHelper.Integer_TYPE | "1"            | "allow"
        ClassHelper.Integer_TYPE | "1 as Integer" | "disallow"
        ClassHelper.int_TYPE     | "1"            | "disallow"
    }

    @Unroll
    def "can not return #returnedValue when enforcing #enforcedType and #allowOrNot boxing"() {
        given:
        def script = """
        def returnsThingsImplicitly() {
            $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)

        and:
        enforceReturnType.allowSubtypes = true
        enforceReturnType.allowBoxing = allowOrNot == "allow"

        and:
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        thrown(MultipleCompilationErrorsException)

        where:
        enforcedType             | returnedValue | allowOrNot
        ClassHelper.Integer_TYPE | "1"           | "disallow"
    }

    @Unroll
    def "can return #returnedValue when enforcing #enforcedType and #allowOrNot unboxing"() {
        given:
        def script = """
        def returnsThingsImplicitly() {
            $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)

        and:
        enforceReturnType.allowSubtypes = true
        enforceReturnType.allowUnboxing = allowOrNot == "allow"

        and:
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        noExceptionThrown()

        where:
        enforcedType         | returnedValue  | allowOrNot
        ClassHelper.int_TYPE | "1 as Integer" | "allow"
    }

    @Unroll
    def "can not return #returnedValue when enforcing #enforcedType and #allowOrNot unboxing"() {
        given:
        def script = """
        def returnsThingsImplicitly() {
            $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)

        and:
        enforceReturnType.allowSubtypes = true
        enforceReturnType.allowUnboxing = allowOrNot == "allow"

        and:
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        thrown(MultipleCompilationErrorsException)

        where:
        enforcedType         | returnedValue  | allowOrNot
        ClassHelper.int_TYPE | "1 as Integer" | "disallow"
    }

    def "Missing else-branch results in error when enforing #enforcedType"() {
        given:
        def script = """
        def returnsInt() {
            if (1 < 2) {
                return $returnedValue
            }
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        thrown(MultipleCompilationErrorsException)

        where:
        enforcedType         | returnedValue | shouldPass
        ClassHelper.int_TYPE | "1"           | false
    }


    def "Missing else-branch results in no error when enforing #enforcedType"() {
        given:
        def script = """
        def returnsInt() {
            if (1 < 2) {
                return $returnedValue
            }
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        noExceptionThrown()

        where:
        enforcedType            | returnedValue | shouldPass
        ClassHelper.OBJECT_TYPE | "1"           | false
    }

    @Unroll
    def "Can return subtype #subtype when enforcing #enforcedType and allowing subtypes"() {
        given:
        def script = """
        def returnsThings() {
            return $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        EnforceReturnType enforceReturnType = new EnforceReturnType(enforcedType)

        and:
        enforceReturnType.allowSubtypes = true

        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        noExceptionThrown()

        where:
        enforcedType             | returnedValue  | subtype
        ClassHelper.make(Number) | "1 as Integer" | ClassHelper.Integer_TYPE
        ClassHelper.make(Number) | "1L as Long"   | ClassHelper.Long_TYPE
    }

    @Unroll
    def "Can not return subtype #subtype when enforcing #enforcedType and disallowing subtypes"() {
        given:
        def script = """
        def returnsThings() {
            return $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        EnforceReturnType enforceReturnType = new EnforceReturnType(enforcedType)

        and:
        enforceReturnType.allowSubtypes = false

        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        thrown(MultipleCompilationErrorsException)

        where:
        enforcedType             | returnedValue  | subtype
        ClassHelper.make(Number) | "1 as Integer" | ClassHelper.Integer_TYPE
        ClassHelper.make(Number) | "1L as Long"   | ClassHelper.Long_TYPE
    }

    @Unroll
    def "Can not return #type when enforcing subtype #enforcedType"() {
        given:
        def script = """
        def returnsThings() {
            return $returnedValue
        }
        """

        and:
        def configuration = new CompilerConfiguration()
        def enforceReturnType = new EnforceReturnType(enforcedType)
        configuration.addCompilationCustomizers(new ASTTransformationCustomizer(enforceReturnType))
        def shell = new GroovyShell(new Binding(), configuration)

        when:
        shell.parse(script)

        then:
        thrown(MultipleCompilationErrorsException)

        where:
        enforcedType          | returnedValue  | type
        ClassHelper.int_TYPE  | "1 as Number"  | ClassHelper.make(Number)
        ClassHelper.long_TYPE | "1L as Number" | ClassHelper.make(Number)
    }

    def createShell() {
        def configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer)
        def shell = new GroovyShell(new Binding(), configuration)
        return shell
    }
}
