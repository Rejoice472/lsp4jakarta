package org.eclipse.lsp4jakarta.jdt.internal.websocket;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;

public class PlatformVersionFinder {

	enum PlatformVersion {
        EE_11(11, "Jakarta EE 11"),
        EE_10(10, "Jakarta EE 10"),
        EE_9(9, "Jakarta EE 9 / 9.1"),
        EE_8(8, "Jakarta EE 8"),
        UNKNOWN(0, "Unknown / Pre-Jakarta EE 8");

        final int level;
        final String label;

        PlatformVersion(int level, String label) {
            this.level = level;
            this.label = label;
        }
    }
	
	public static void analyzeClasspath(IClasspathEntry[] entries) {
        
        // Regex to match JAR names and versions: matches "name-version.jar"
        Pattern jarPattern = Pattern.compile("([^/\\\\]+)-([0-9\\.]+[^/\\\\]*)\\.jar$");

        PlatformVersion detectedVersion = PlatformVersion.UNKNOWN;
        

        for (IClasspathEntry entry : entries) {
            IPath path = entry.getPath();
            String line = path.toOSString();
            System.out.println("Classpath entry: " + line);
            
            
            if (line.isEmpty() || !line.endsWith(".jar")) continue;

            Matcher matcher = jarPattern.matcher(line);
            if (matcher.find()) {
                String artifactName = matcher.group(1);
                String version = matcher.group(2);

                // Priority 1: Check for the Jakarta EE Platform base Jar
                if (artifactName.contains("jakartaee-api") || artifactName.contains("jakartaee-web-api")){
                    PlatformVersion v = getFromPFList(version);
                    if (v.level > detectedVersion.level) {
                        detectedVersion = v;
                        
                    }
                }
                
                
                // Prority 2: check module version
                PlatformVersion moduleVersion = identifyModuleVersion(artifactName, version);
                if (moduleVersion.level > detectedVersion.level) {
                    detectedVersion = moduleVersion;
                    
                }
            }
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("Identified Platform Version: " + detectedVersion.label+"-"+detectedVersion.level);
        System.out.println("------------------------------------------------------------------");
   }
        
        
    
	
	private static PlatformVersion getFromPFList(String version) {
        if (version.startsWith("11.")) return PlatformVersion.EE_11;
        if (version.startsWith("10.")) return PlatformVersion.EE_10;
        if (version.startsWith("9.")) return PlatformVersion.EE_9;
        if (version.startsWith("8.")) return PlatformVersion.EE_8;
        return PlatformVersion.UNKNOWN;
    }
	
	private static PlatformVersion identifyModuleVersion(String name, String version) {
        double ver = parseVersionDouble(version);

        // --- SERVLET API ---
        if (name.contains("servlet-api") || name.equals("jakarta.servlet")) {
            if (ver >= 6.1) return PlatformVersion.EE_11;
            if (ver >= 6.0) return PlatformVersion.EE_10;
            if (ver >= 5.0) return PlatformVersion.EE_9;
            if (ver >= 4.0) return PlatformVersion.EE_8;
        }

        // --- JPA (PERSISTENCE) ---
        if (name.contains("persistence-api") || name.contains("jakarta.persistence")) {
            if (ver >= 3.2) return PlatformVersion.EE_11;
            if (ver >= 3.1) return PlatformVersion.EE_10;
            if (ver >= 3.0) return PlatformVersion.EE_9;
            if (ver >= 2.2) return PlatformVersion.EE_8;
        }

        
        return PlatformVersion.UNKNOWN;
    }

    private static double parseVersionDouble(String version) {
        try {
            String[] parts = version.split("\\.");
            if (parts.length >= 2) {
                return Double.parseDouble(parts[0] + "." + parts[1]);
            }
            return Double.parseDouble(parts[0]);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
