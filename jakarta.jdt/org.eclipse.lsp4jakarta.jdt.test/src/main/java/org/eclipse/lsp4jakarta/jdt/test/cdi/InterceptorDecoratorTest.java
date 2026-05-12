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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.test.cdi;

import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaCodeAction;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.assertJavaDiagnostics;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.ca;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.createCodeActionParams;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.d;
import static org.eclipse.lsp4jakarta.jdt.test.core.JakartaForJavaAssert.te;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import com.google.gson.Gson;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.test.core.BaseJakartaTest;
import org.junit.Test;

/**
 * Tests for CDI Interceptor and Decorator scope validation.
 */
public class InterceptorDecoratorTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void interceptorDecoratorScopes() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/InterceptorDecoratorScopes.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Invalid: @Interceptor with @ApplicationScoped
        Diagnostic interceptorWithAppScoped = d(12, 13, 39,
                                                "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Invalid: @Decorator with @RequestScoped
        Diagnostic decoratorWithReqScoped = d(18, 6, 39,
                                              "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                              DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Invalid: @Interceptor with @Dependent and @SessionScoped (has invalid scope)
        // This triggers TWO diagnostics
        Diagnostic interceptorWithMultipleScopes = d(37, 6, 42,
                                                     "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Second diagnostic for having multiple scopes
        Diagnostic interceptorMultipleScopesError = d(37, 6, 42,
                                                      "Scope type annotations must be specified by a managed bean class at most once.",
                                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        interceptorMultipleScopesError.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent", "jakarta.enterprise.context.SessionScoped")));

        // Diagnostics are returned in reverse line order (37, 18, 12)
        // For line 37, InvalidNumberOfScopedAnnotationsByManagedBean comes before InvalidInterceptorOrDecorator
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, interceptorMultipleScopesError,
                              interceptorWithMultipleScopes, decoratorWithReqScoped, interceptorWithAppScoped);

        // Test quick fix for @Interceptor with @ApplicationScoped - replace with @Dependent
        JakartaJavaCodeActionParams interceptorAppScopedParams = createCodeActionParams(uri, interceptorWithAppScoped);
        TextEdit interceptorAppScopedEdit = te(10, 0, 12, 0, "@Dependent\n");
        CodeAction interceptorAppScopedAction = ca(uri, "Replace current scope with @Dependent",
                                                   interceptorWithAppScoped, interceptorAppScopedEdit);
        assertJavaCodeAction(interceptorAppScopedParams, IJDT_UTILS, interceptorAppScopedAction);

        // Test quick fix for @Decorator with @RequestScoped - replace with @Dependent
        JakartaJavaCodeActionParams decoratorReqScopedParams = createCodeActionParams(uri, decoratorWithReqScoped);
        TextEdit decoratorReqScopedEdit = te(16, 0, 17, 14, "@Dependent");
        CodeAction decoratorReqScopedAction = ca(uri, "Replace current scope with @Dependent", decoratorWithReqScoped,
                                                 decoratorReqScopedEdit);
        assertJavaCodeAction(decoratorReqScopedParams, IJDT_UTILS, decoratorReqScopedAction);

        // Test quick fix for @Interceptor with multiple scopes - replace with @Dependent
        JakartaJavaCodeActionParams interceptorMultiScopesParams = createCodeActionParams(uri,
                                                                                          interceptorWithMultipleScopes);
        TextEdit interceptorMultiScopesEdit = te(34, 0, 36, 14, "@Dependent");
        CodeAction interceptorMultiScopesAction = ca(uri, "Replace current scope with @Dependent",
                                                     interceptorWithMultipleScopes, interceptorMultiScopesEdit);
        assertJavaCodeAction(interceptorMultiScopesParams, IJDT_UTILS, interceptorMultiScopesAction);
    }
}

// Made with Bob
