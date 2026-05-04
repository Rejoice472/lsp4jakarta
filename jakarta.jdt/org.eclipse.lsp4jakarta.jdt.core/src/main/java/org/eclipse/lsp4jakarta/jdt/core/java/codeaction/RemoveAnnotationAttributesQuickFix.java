/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.codeaction.ICodeActionId;
import org.eclipse.lsp4jakarta.jdt.core.ASTNodeUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ChangeCorrectionProposal;
import org.eclipse.lsp4jakarta.jdt.core.java.corrections.proposal.ModifyAnnotationProposal;

/**
 * Removes specified attributes from annotations.
 * Follows the pattern of RemoveMethodParamAnnotationQuickFix but modifies attributes instead of removing annotations.
 */
public abstract class RemoveAnnotationAttributesQuickFix implements IJavaCodeActionParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(RemoveAnnotationAttributesQuickFix.class.getName());

    /** Map key to retrieve a list of annotations. */
    protected static final String ANNOTATIONS_KEY = "annotations";

    /** Map key to retrieve parameter names. */
    protected static final String PARAMETER_NAME_KEY = "parameter.name";

    /** Map key to retrieve attributes to remove. */
    protected static final String ATTRIBUTES_KEY = "attributes";

    /** Annotations to check. */
    String[] annotations;

    /** Attributes to remove. */
    String[] attributes;

    public RemoveAnnotationAttributesQuickFix(String[] annotations, String... attributes) {
        this.annotations = annotations;
        this.attributes = attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getParticipantId() {
        return RemoveAnnotationAttributesQuickFix.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<? extends CodeAction> getCodeActions(JavaCodeActionContext context, Diagnostic diagnostic,
                                                     IProgressMonitor monitor) throws CoreException {
        List<CodeAction> codeActions = new ArrayList<>();
        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();
        List<SingleVariableDeclaration> parameters = (List<SingleVariableDeclaration>) parentNode.parameters();

        for (SingleVariableDeclaration parameter : parameters) {
            // Collect all matching annotations that have attributes to remove
            List<String> annotationsToProcess = findAnnotationsWithAttributesToRemove(parameter);
            
            // Create a code action for each annotation
            for (String annotation : annotationsToProcess) {
                createCodeAction(diagnostic, context, codeActions, parameter, annotation);
            }
        }

        return codeActions;
    }

    /**
     * Finds all annotations on a parameter that match the target annotations
     * and have attributes that need to be removed.
     *
     * @param parameter The parameter to check
     * @return List of qualified annotation names that have attributes to remove
     */
    private List<String> findAnnotationsWithAttributesToRemove(SingleVariableDeclaration parameter) {
        List<String> result = new ArrayList<>();
        
        for (Annotation annotation : getParameterAnnotations(parameter)) {
            ITypeBinding typeBinding = annotation.resolveTypeBinding();
            if (typeBinding != null && isTargetAnnotation(typeBinding.getQualifiedName())
                && hasAttributesToRemove(annotation)) {
                result.add(typeBinding.getQualifiedName());
            }
        }
        return result;
    }

    /**
     * Gets all annotations from a parameter's modifiers.
     *
     * @param parameter The parameter to check
     * @return List of annotations
     */
    @SuppressWarnings("unchecked")
    private List<Annotation> getParameterAnnotations(SingleVariableDeclaration parameter) {
        List<Annotation> annotations = new ArrayList<>();
        List<ASTNode> modifiers = (List<ASTNode>) parameter.getStructuralProperty(SingleVariableDeclaration.MODIFIERS2_PROPERTY);
        
        for (ASTNode modifier : modifiers) {
            if (ASTNodeUtils.isAnnotation(modifier)) {
                annotations.add((Annotation) modifier);
            }
        }
        return annotations;
    }

    /**
     * Checks if the given annotation name matches any of the target annotations.
     *
     * @param annotationName The fully qualified annotation name
     * @return true if it matches a target annotation
     */
    private boolean isTargetAnnotation(String annotationName) {
        return Arrays.asList(this.annotations).contains(annotationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CodeAction resolveCodeAction(JavaCodeActionResolveContext context) {
        CodeAction toResolve = context.getUnresolved();
        ASTNode node = context.getCoveredNode();
        MethodDeclaration parentNode = (MethodDeclaration) node.getParent();

        CodeActionResolveData data = (CodeActionResolveData) toResolve.getData();
        String paramName = (String) data.getExtendedDataEntry(PARAMETER_NAME_KEY);
        List<String> annotationsList = (List<String>) data.getExtendedDataEntry(ANNOTATIONS_KEY);
        List<String> attributesList = (List<String>) data.getExtendedDataEntry(ATTRIBUTES_KEY);

        String[] annotationsArray = annotationsList.toArray(new String[0]);

        IBinding binding = getParameterBinding(parentNode, paramName, annotationsArray);

        if (binding != null) {
            String label = getLabel(annotationsArray[0], attributesList.toArray(new String[0]));
            // Use the constructor that takes attributesToAdd and attributesToRemove
            // Pass empty list for attributesToAdd and our attributes for attributesToRemove
            ChangeCorrectionProposal proposal = new ModifyAnnotationProposal(label, context.getCompilationUnit(), context.getASTRoot(), binding, 0, annotationsArray[0], new ArrayList<String>(), attributesList);

            try {
                toResolve.setEdit(context.convertToWorkspaceEdit(proposal));
            } catch (CoreException e) {
                LOGGER.log(Level.SEVERE, "Unable to resolve code action to remove annotation attributes", e);
            }
        }

        return toResolve;
    }

    /**
     * Creates a code action for removing attributes from an annotation on a parameter.
     */
    @SuppressWarnings("unchecked")
    protected void createCodeAction(Diagnostic diagnostic, JavaCodeActionContext context,
                                    List<CodeAction> codeActions, SingleVariableDeclaration parameter,
                                    String annotation) {
        String label = getLabel(annotation, attributes);

        ExtendedCodeAction codeAction = new ExtendedCodeAction(label);
        codeAction.setRelevance(0);
        codeAction.setKind(CodeActionKind.QuickFix);
        codeAction.setDiagnostics(Arrays.asList(diagnostic));

        Map<String, Object> extendedData = new HashMap<String, Object>();
        extendedData.put(PARAMETER_NAME_KEY, parameter.getName().getIdentifier());
        extendedData.put(ANNOTATIONS_KEY, Arrays.asList(annotation));
        extendedData.put(ATTRIBUTES_KEY, Arrays.asList(attributes));

        codeAction.setData(new CodeActionResolveData(context.getUri(), getParticipantId(), context.getParams().getRange(), extendedData, context.getParams().isResourceOperationSupported(), context.getParams().isCommandConfigurationUpdateSupported(), getCodeActionId()));

        codeActions.add(codeAction);
    }

    /**
     * Gets the binding for a parameter by name and annotation.
     */
    @SuppressWarnings("unchecked")
    protected IBinding getParameterBinding(MethodDeclaration method, String paramName, String[] annotations) {
        List<SingleVariableDeclaration> parameters = method.parameters();

        for (SingleVariableDeclaration param : parameters) {
            if (param.getName().getIdentifier().equals(paramName)) {
                for (Annotation annotation : getParameterAnnotations(param)) {
                    ITypeBinding typeBinding = annotation.resolveTypeBinding();
                    if (typeBinding != null && Arrays.asList(annotations).contains(typeBinding.getQualifiedName())) {
                        return param.resolveBinding();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if the given annotation has any of the attributes that need to be removed.
     * This prevents code actions from appearing after attributes have already been removed.
     * Subclasses can override shouldRemoveAttribute() to add value-based checks.
     *
     * @param annotation The annotation to check
     * @return true if the annotation has at least one attribute to remove, false otherwise
     */
    @SuppressWarnings("unchecked")
    protected boolean hasAttributesToRemove(Annotation annotation) {
        if (!(annotation instanceof NormalAnnotation)) {
            return false; // Only NormalAnnotations have name-value pairs
        }

        NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
        List<MemberValuePair> values = normalAnnotation.values();

        // Check if any target attribute exists and should be removed
        for (String attributeToRemove : this.attributes) {
            for (MemberValuePair mvp : values) {
                if (mvp.getName().getFullyQualifiedName().equals(attributeToRemove)
                    && shouldRemoveAttribute(mvp)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a specific attribute should be removed based on its value.
     * Default: remove regardless of value. Override to add value checks.
     *
     * @param memberValuePair The member value pair to check
     * @return true if the attribute should be removed
     */
    protected boolean shouldRemoveAttribute(MemberValuePair memberValuePair) {
        return true;
    }

    /**
     * Returns the label for the code action.
     *
     * @param annotation The fully qualified annotation name
     * @param attributes The attributes to remove
     * @return The label for the code action
     */
    protected abstract String getLabel(String annotation, String[] attributes);

    /**
     * Returns the id for this code action.
     *
     * @return the id for this code action
     */
    protected abstract ICodeActionId getCodeActionId();
}

// Made with Bob
