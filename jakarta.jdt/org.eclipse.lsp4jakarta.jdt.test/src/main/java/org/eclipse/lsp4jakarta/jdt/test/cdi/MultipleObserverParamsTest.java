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

public class MultipleObserverParamsTest extends BaseJakartaTest {

    protected static IJDTUtils IJDT_UTILS = JDTUtilsLSImpl.getInstance();

    @Test
    public void multipleObserverParams() throws Exception {
        IJavaProject javaProject = loadJavaProject("jakarta-sample", "");
        IFile javaFile = javaProject.getProject().getFile(new Path("src/main/java/io/openliberty/sample/jakarta/cdi/MultipleObserverParams.java"));
        String uri = javaFile.getLocation().toFile().toURI().toString();

        JakartaJavaDiagnosticsParams diagnosticsParams = new JakartaJavaDiagnosticsParams();
        diagnosticsParams.setUris(Arrays.asList(uri));

        // Invalid: Two parameters, each with @Observes
        Diagnostic twoObserves = d(15, 16, 34,
                                   "Parameters event1, event2 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                   DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: One parameter with @Observes, another with @ObservesAsync
        Diagnostic observesAndObservesAsync = d(19, 16, 47,
                                                "Parameters event1, event2 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                                DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        // Invalid: Three parameters - one with @Observes, two with @ObservesAsync
        Diagnostic threeObserves = d(23, 16, 36,
                                     "Parameters event1, event2, event3 are annotated with @Observes or @ObservesAsync, but a method cannot contain more than one such parameter.",
                                     DiagnosticSeverity.Error, "jakarta-cdi", "InvalidMultipleObserverParams");

        assertJavaDiagnostics(diagnosticsParams, IJDT_UTILS, twoObserves, observesAndObservesAsync, threeObserves);

        JakartaJavaCodeActionParams codeActionParams1 = createCodeActionParams(uri, twoObserves);
        TextEdit te1 = te(15, 35, 15, 45, "");
        CodeAction ca1 = ca(uri, "Remove the '@Observes' modifier from parameter 'event1'", twoObserves, te1);
        TextEdit te2 = te(15, 60, 15, 70, "");
        CodeAction ca2 = ca(uri, "Remove the '@Observes' modifier from parameter 'event2'", twoObserves, te2);

        assertJavaCodeAction(codeActionParams1, IJDT_UTILS, ca1, ca2);
        JakartaJavaCodeActionParams codeActionParams2 = createCodeActionParams(uri, observesAndObservesAsync);
        TextEdit te3 = te(19, 48, 19, 58, "");
        CodeAction ca3 = ca(uri, "Remove the '@Observes' modifier from parameter 'event1'", observesAndObservesAsync, te3);
        TextEdit te4 = te(19, 73, 19, 88, "");
        CodeAction ca4 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'event2'", observesAndObservesAsync, te4);

        assertJavaCodeAction(codeActionParams2, IJDT_UTILS, ca3, ca4);

        JakartaJavaCodeActionParams codeActionParams3 = createCodeActionParams(uri, threeObserves);
        TextEdit te5 = te(23, 37, 23, 47, "");
        CodeAction ca5 = ca(uri, "Remove the '@Observes' modifier from parameter 'event1'", threeObserves, te5);
        TextEdit te6 = te(23, 62, 23, 77, "");
        CodeAction ca6 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'event2'", threeObserves, te6);
        TextEdit te7 = te(23, 92, 23, 107, "");
        CodeAction ca7 = ca(uri, "Remove the '@ObservesAsync' modifier from parameter 'event3'", threeObserves, te7);

        assertJavaCodeAction(codeActionParams3, IJDT_UTILS, ca5, ca6, ca7);
    }
}
