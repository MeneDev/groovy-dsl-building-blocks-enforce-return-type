package de.menedev.groovy.dsl.buildingblocks

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.StaticTypesTransformation
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor

import java.util.function.Predicate

@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
@CompileStatic
class EnforceReturnType extends StaticTypesTransformation {

    final ClassNode enforcedType
    final Predicate<MethodNode> checkMethod

    boolean allowSubtypes = true
    boolean allowBoxing = true
    boolean allowUnboxing = true

    EnforceReturnType(ClassNode enforcedType) {
        this(enforcedType, { MethodNode node ->
            // sensible (?) default?
            !node.synthetic
        } as Predicate<MethodNode>)
    }

    EnforceReturnType(ClassNode enforcedType, Predicate<MethodNode> checkMethod) {
        this.enforcedType = enforcedType
        this.checkMethod = checkMethod
    }

    @Override
    protected StaticTypeCheckingVisitor newVisitor(SourceUnit unit, ClassNode node) {

        def visitor = super.newVisitor(unit, node)
        def typeCheckingExtension = new EnforceReturnTypeCheckingExtension(visitor, enforcedType, checkMethod)
        typeCheckingExtension.allowSubtypes = allowSubtypes
        typeCheckingExtension.allowBoxing = allowBoxing
        typeCheckingExtension.allowUnboxing = allowUnboxing
        visitor.addTypeCheckingExtension(typeCheckingExtension)
        return visitor
    }

    /*
     * When an ASTTransformation is added via Annotation, the nodes array gets populated.
     * Here we assume it's added to a GroovyShell directly.
     * Other than that it's copied from the super-class.
     */
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        ClassNode classNode = source.getAST().classes[0]

        StaticTypeCheckingVisitor visitor = newVisitor(source, classNode)
        visitor.setCompilationUnit(compilationUnit)
        visitor.initialize()
        visitor.visitClass(classNode)
        visitor.performSecondPass()
    }
}
