package de.menedev.groovy.dsl.buildingblocks

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.classgen.ReturnAdder
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor

import java.util.function.Predicate

@CompileStatic
class EnforceReturnTypeCheckingExtension extends AbstractTypeCheckingExtension {

    final ClassNode enforcedType
    final Predicate<MethodNode> checkMethod

    boolean allowSubtypes = true
    boolean allowBoxing = true
    boolean allowUnboxing = true

    EnforceReturnTypeCheckingExtension(StaticTypeCheckingVisitor typeCheckingVisitor, ClassNode enforcedType, Predicate<MethodNode> checkMethod) {
        super(typeCheckingVisitor)
        this.enforcedType = enforcedType
        this.checkMethod = checkMethod
    }

    @Override
    void afterVisitMethod(MethodNode methodNode) {
        if (!checkMethod.test(methodNode)) {
            return
        }

        ReturnAdder adder = new ReturnAdder()
        adder.visitMethod(methodNode)

        def codeVisitorSupport = new CodeVisitorSupport() {

            @Override
            void visitReturnStatement(ReturnStatement statement) {
                def expression = statement.expression
                def inferredType = getType(expression)
                boolean isAllowedType = inferredType == enforcedType
                if (!isAllowedType && allowSubtypes) {
                    isAllowedType = StaticTypeCheckingSupport.checkCompatibleAssignmentTypes(enforcedType, inferredType)
                }

                if(isAllowedType && !allowBoxing) {
                    if (ClassHelper.isPrimitiveType(inferredType)) {
                        isAllowedType = ClassHelper.isPrimitiveType(enforcedType)
                    }
                }

                if(isAllowedType && !allowUnboxing) {
                    if (ClassHelper.isPrimitiveType(enforcedType)) {
                        isAllowedType = ClassHelper.isPrimitiveType(inferredType)
                    }
                }

                if (!isAllowedType) {
                    def node = expression
                    if (node.columnNumber < 0 || node.lineNumber < 0) {
                        // generated expressions can have no source position
                        node = methodNode
                    }
                    addStaticTypeError("Method $methodNode.name must return $enforcedType but inferred ${inferredType.redirect()}", node)
                }
            }
        }

        methodNode.code.visit(codeVisitorSupport)
    }
}
