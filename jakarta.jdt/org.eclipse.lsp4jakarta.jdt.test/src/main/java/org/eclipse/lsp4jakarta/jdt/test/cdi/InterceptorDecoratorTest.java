/*******************************************************************************
 * Copyright (c) 2021, 2026 IBM Corporation and others.
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
        Diagnostic interceptorWithAppScoped = d(15, 24, 63,
                                                "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Invalid: @Decorator with @RequestScoped
        Diagnostic decoratorWithReqScoped = d(21, 24, 57,
                                              "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                              DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Invalid: @Interceptor with @Dependent and @SessionScoped (has invalid scope)
        // This triggers TWO diagnostics
        Diagnostic interceptorWithMultipleScopes = d(40, 24, 60,
                                                     "Interceptors and decorators must be annotated with the @Dependent scope. Any other scope is invalid.",
                                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidInterceptorOrDecorator");

        // Second diagnostic for having multiple scopes
        Diagnostic interceptorMultipleScopesError = d(40, 24, 60,
                                                      "Scope type annotations must be specified by a managed bean class at most once.",
                                                      DiagnosticSeverity.Error, "jakarta-cdi", "InvalidNumberOfScopedAnnotationsByManagedBean");
        interceptorMultipleScopesError.setData(new Gson().toJsonTree(Arrays.asList("jakarta.enterprise.context.Dependent", "jakarta.enterprise.context.SessionScoped")));

        // Diagnostics are returned in reverse line order (40, 21, 15)
        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, interceptorWithMultipleScopes,
                              interceptorMultipleScopesError, decoratorWithReqScoped, interceptorWithAppScoped);

        // Test quick fix for @Interceptor with @ApplicationScoped - replace with @Dependent
        JakartaJavaCodeActionParams interceptorAppScopedParams = createCodeActionParams(uri, interceptorWithAppScoped);
        TextEdit interceptorAppScopedEdit = te(13, 4, 15, 4, "@Dependent\n\t");
        CodeAction interceptorAppScopedAction = ca(uri, "Replace current scope with @Dependent",
                                                   interceptorWithAppScoped, interceptorAppScopedEdit);
        assertJavaCodeAction(interceptorAppScopedParams, IJDT_UTILS, interceptorAppScopedAction);

        // Test quick fix for @Decorator with @RequestScoped - replace with @Dependent
        JakartaJavaCodeActionParams decoratorReqScopedParams = createCodeActionParams(uri, decoratorWithReqScoped);
        TextEdit decoratorReqScopedEdit = te(19, 4, 21, 4, "@Dependent\n\t");
        CodeAction decoratorReqScopedAction = ca(uri, "Replace current scope with @Dependent", decoratorWithReqScoped,
                                                 decoratorReqScopedEdit);
        assertJavaCodeAction(decoratorReqScopedParams, IJDT_UTILS, decoratorReqScopedAction);

        // Test quick fix for @Interceptor with multiple scopes - replace with @Dependent
        JakartaJavaCodeActionParams interceptorMultiScopesParams = createCodeActionParams(uri,
                                                                                          interceptorWithMultipleScopes);
        TextEdit interceptorMultiScopesEdit = te(37, 4, 40, 4, "@Dependent\n\t");
        CodeAction interceptorMultiScopesAction = ca(uri, "Replace current scope with @Dependent",
                                                     interceptorWithMultipleScopes, interceptorMultiScopesEdit);
        assertJavaCodeAction(interceptorMultiScopesParams, IJDT_UTILS, interceptorMultiScopesAction);
    }
}

// Made with Bob
