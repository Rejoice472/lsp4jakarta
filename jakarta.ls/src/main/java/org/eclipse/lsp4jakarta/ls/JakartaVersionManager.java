/*******************************************************************************
* Copyright (c) 2020, 2023 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.ls;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages Jakarta EE version persistence to project directory.
 * Stores the selected version in a .jakarta-version file in the project root.
 * Uses Java NIO for cross-platform compatibility (Windows, Linux, macOS).
 */
public class JakartaVersionManager {

    private static final Logger LOGGER = Logger.getLogger(JakartaVersionManager.class.getName());
    private static final String VERSION_FILE_NAME = ".jakarta-version";

    /**
     * Reads the Jakarta EE version from the project's version file.
     * Uses Java NIO for cross-platform file reading.
     *
     * @param projectUri The project URI
     * @return The version string if found, null otherwise
     */
    public static String readVersion(String projectUri) {
        if (projectUri == null) {
            return null;
        }

        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null || !Files.exists(versionFilePath)) {
                return null;
            }

            // Read all lines using NIO (handles different line endings across platforms)
            List<String> lines = Files.readAllLines(versionFilePath, StandardCharsets.UTF_8);
            if (!lines.isEmpty()) {
                String version = lines.get(0).trim();
                if (!version.isEmpty()) {
                    LOGGER.info("Read Jakarta EE version " + version + " from " + versionFilePath);
                    return version;
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Failed to read Jakarta version file for project " + projectUri + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Writes the Jakarta EE version to the project's version file.
     * Uses Java NIO for cross-platform file writing with proper line endings.
     *
     * @param projectUri The project URI
     * @param version The version to write
     * @return true if successful, false otherwise
     */
    public static boolean writeVersion(String projectUri, String version) {
        if (projectUri == null || version == null) {
            return false;
        }

        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null) {
                return false;
            }

            // Ensure parent directory exists
            Path parentDir = versionFilePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // Write using NIO with platform-specific line separator
            String content = version + System.lineSeparator();
            Files.write(versionFilePath, content.getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE);

            LOGGER.info("Wrote Jakarta EE version " + version + " to " + versionFilePath);
            return true;
        } catch (IOException e) {
            LOGGER.severe("Failed to write Jakarta version file for project " + projectUri + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes the version file for a project.
     * Uses Java NIO for cross-platform file deletion.
     *
     * @param projectUri The project URI
     * @return true if deleted or didn't exist, false on error
     */
    public static boolean deleteVersion(String projectUri) {
        if (projectUri == null) {
            return true;
        }

        try {
            Path versionFilePath = getVersionFilePath(projectUri);
            if (versionFilePath == null) {
                return true;
            }

            if (Files.exists(versionFilePath)) {
                Files.delete(versionFilePath);
                LOGGER.info("Deleted Jakarta version file: " + versionFilePath);
            }
            return true;
        } catch (IOException e) {
            LOGGER.warning("Failed to delete Jakarta version file for project " + projectUri + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets the path to the version file for a project.
     * Handles various URI formats and normalizes paths for cross-platform compatibility.
     *
     * @param projectUri The project URI (can be file:// URI or plain path)
     * @return The path to the version file, or null if invalid URI
     */
    private static Path getVersionFilePath(String projectUri) {
        try {
            Path projectPath = uriToPath(projectUri);
            if (projectPath == null) {
                return null;
            }

            // If it's a file, get its parent directory
            if (Files.isRegularFile(projectPath)) {
                projectPath = projectPath.getParent();
            }

            // Resolve the version file name (handles path separators correctly on all platforms)
            return projectPath.resolve(VERSION_FILE_NAME);
        } catch (Exception e) {
            LOGGER.warning("Failed to resolve version file path for URI " + projectUri + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Converts a URI string to a Path, handling various formats.
     * Supports file:// URIs and plain file paths on Windows, Linux, and macOS.
     *
     * @param uriString The URI string
     * @return The Path object, or null if conversion fails
     */
    private static Path uriToPath(String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return null;
        }

        try {
            // Handle file:// URIs
            if (uriString.startsWith("file://") || uriString.startsWith("file:")) {
                URI uri = new URI(uriString);
                return Paths.get(uri);
            }

            // Handle plain paths (works on all platforms)
            return Paths.get(uriString).toAbsolutePath().normalize();
        } catch (URISyntaxException | IllegalArgumentException e) {
            LOGGER.warning("Failed to convert URI to Path: " + uriString + " - " + e.getMessage());
            return null;
        }
    }
}
