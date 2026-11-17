/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.commons;

import java.util.List;

/**
 * Jakarta Java Project labels
 *
 * @author Angelo ZERR
 *
 */
public class JakartaJavaProjectLabelsParams {

    private String uri;

    private List<String> types;

    private boolean includeJakartaVersions = true;

    /**
     * Returns the Java file uri.
     *
     * @return the Java file uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Set the Java file uri.
     *
     * @param uri the Java file uri.
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Returns the Java types list to check.
     *
     * <p>
     * If the owner Java project of the Java file URI contains some type in the
     * classpath, it will return the type as label in
     * {@link ProjectLabelInfoEntry#getLabels()}
     * </p>
     *
     * @return the Java types list to check
     */
    public List<String> getTypes() {
        return types;
    }

    /**
     * Set the Java types list to check.
     *
     * @param types the Java types list to check.
     */
    public void setTypes(List<String> types) {
        this.types = types;
    }

    /**
     * Returns true if the Jakarta EE versions of the project must be included in
     * the project information (this analyzes the project classpath), false if the
     * caller already knows the version (from its cache or version file) and does
     * not need them. Defaults to true.
     *
     * @return true if the Jakarta EE versions must be included
     */
    public boolean isIncludeJakartaVersions() {
        return includeJakartaVersions;
    }

    /**
     * Set whether the Jakarta EE versions of the project must be included in the
     * project information.
     *
     * @param includeJakartaVersions false if the caller already knows the version
     */
    public void setIncludeJakartaVersions(boolean includeJakartaVersions) {
        this.includeJakartaVersions = includeJakartaVersions;
    }
}
