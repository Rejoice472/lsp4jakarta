package org.eclipse.lsp4jakarta.version;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum JakartaDiagnostic {

    // --- EE 9 Diagnostics ---
    INVALID_ON_OPEN_PARAMS("InvalidOnOpenParams", JakartaVersion.EE_9, JakartaVersion.EE_10),
    INVALID_ON_CLOSE_PARAMS("InvalidOnCloseParams", JakartaVersion.EE_9),
    PATH_PARAMS_MISSING_FROM_PARAM("PathParamsMissingFromParam", JakartaVersion.EE_9),
    PATH_PARAM_DOES_NOT_MATCH_ENDPOINT_URI("PathParamDoesNotMatchEndpointURI", JakartaVersion.EE_9),
    ON_MESSAGE_DUPLICATE_METHOD("OnMessageDuplicateMethod", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_NO_SLASH("InvalidEndpointPathWithNoStartingSlash", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_RELATIVE("InvalidEndpointPathWithRelativePaths", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_NOT_TEMPLATE("InvalidEndpointPathNotTempleateOrPartialURI", JakartaVersion.EE_9),
    INVALID_ENDPOINT_PATH_DUPLICATE_VAR("InvalidEndpointPathDuplicateVariable", JakartaVersion.EE_9),
    InvalidTypeOfField("InvalidTypeOfField", JakartaVersion.EE_9),
    InvalidReturnTypeOfMethod("InvalidReturnTypeOfMethod", JakartaVersion.EE_9),

    // --- EE 10 Diagnostics ---
    INVALID_DATE_FORMAT("InvalidDateFormat", JakartaVersion.EE_10),
    MISSING_RESOURCE_ANNOTATION("MissingResourceAnnotation", JakartaVersion.EE_10),
    MISSING_RESOURCE_NAME_ATTRIBUTE("MissingResourceNameAttribute", JakartaVersion.EE_10),
    MISSING_RESOURCE_TYPE_ATTRIBUTE("MissingResourceTypeAttribute", JakartaVersion.EE_10),
    POST_CONSTRUCT_PARAMS("PostConstructParams", JakartaVersion.EE_10),
    POST_CONSTRUCT_RETURN_TYPE("PostConstructReturnType", JakartaVersion.EE_10),
    POST_CONSTRUCT_EXCEPTION("PostConstructException", JakartaVersion.EE_10),
    PRE_DESTROY_PARAMS("PreDestroyParams", JakartaVersion.EE_10),
    PRE_DESTROY_STATIC("PreDestroyStatic", JakartaVersion.EE_10),
    PRE_DESTROY_EXCEPTION("PreDestroyException", JakartaVersion.EE_10),

    // --- EE 11 Diagnostics ---
    INVALID_INJECT_FINAL_FIELD("InvalidInjectAnnotationOnFinalField", JakartaVersion.EE_11),
    INVALID_INJECT_INNER_CLASS("InvalidInjectAnnotationOnNonStaticInnerClass", JakartaVersion.EE_11),
    INVALID_INJECT_FINAL_METHOD("InvalidInjectAnnotationOnFinalMethod", JakartaVersion.EE_11),
    INVALID_INJECT_ABSTRACT_METHOD("InvalidInjectAnnotationOnAbstractMethod", JakartaVersion.EE_11),
    INVALID_INJECT_STATIC_METHOD("InvalidInjectAnnotationOnStaticMethod", JakartaVersion.EE_11),
    INVALID_INJECT_GENERIC_METHOD("InvalidInjectAnnotationOnGenericMethod", JakartaVersion.EE_11),
    INVALID_INJECT_MULTIPLE_CONSTRUCTORS("InvalidInjectAnnotationOnMultipleConstructors", JakartaVersion.EE_11),
    INJECTION_POINT_INVALID_PRIMITIVE("InjectionPointInvalidPrimitiveBean", JakartaVersion.EE_11),
    INJECTION_POINT_INVALID_INNER_CLASS("InjectionPointInvalidInnerClassBean", JakartaVersion.EE_11),
    INJECTION_POINT_INVALID_ABSTRACT_CLASS("InjectionPointInvalidAbstractClassBean", JakartaVersion.EE_11),
    INJECTION_POINT_INVALID_EXTENSION("InjectionPointInvalidExtensionProviderBean", JakartaVersion.EE_11),
    INJECTION_POINT_INVALID_VETOED("InjectionPointInvalidVetoedClassBean", JakartaVersion.EE_11),
    INJECTION_POINT_INVALID_CONSTRUCTOR("InjectionPointInvalidConstructorBean", JakartaVersion.EE_11);

    private final String code;
    private final JakartaVersion minVersion;
    private final JakartaVersion maxVersion;

    JakartaDiagnostic(String code, JakartaVersion minVersion) {
        this(code, minVersion, null);
    }

    JakartaDiagnostic(String code, JakartaVersion minVersion, JakartaVersion maxVersion) {
        this.code = code;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public String getCode() {
        return code;
    }

    public JakartaVersion getMinVersion() {
        return minVersion;
    }

    /**
     * Checks if this diagnostic is relevant for the given project version.
     * Usually, a rule introduced in EE9 is also valid in EE10 and EE11.
     */
    public boolean isApplicableTo(JakartaVersion projectVersion) {
        if (projectVersion == JakartaVersion.UNKNOWN)
            return false;

        int projectLevel = projectVersion.getLevel();
        int minLevel = this.minVersion.getLevel();

        // 1. Check if the project is too old for this rule
        if (projectLevel < minLevel) {
            return false;
        }

        // 2. Check if the project is too new for this rule (if a max limit exists)
        if (this.maxVersion != null) {
            int maxLevel = this.maxVersion.getLevel();
            if (projectLevel > maxLevel) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper to get all diagnostics for a specific version strictly.
     */
    public static List<JakartaDiagnostic> getRulesForExactly(JakartaVersion version) {
        return Arrays.stream(values()).filter(d -> d.minVersion == version).collect(Collectors.toList());
    }

    public static JakartaDiagnostic getByCodeOrNull(String code) {
        return Arrays.stream(values()).filter(d -> d.code.equals(code)).findFirst().orElse(null);
    }
}