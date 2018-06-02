package se.tink.backend.core;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Parses the Tink app user agent. Use a UserAgentStringParser for parsing general user agents.
 */
public class TinkUserAgent {
    // User-agent from clients have the format of "{appname}/{appversion} {osname};{osversion} {device}"
    private static Pattern appVersionPattern = Pattern.compile("(\\/[0-9](.[0-9]+)+)([\\- ]([\\w\\-]+))? \\(");

    private static Pattern userAgentPattern = Pattern
            .compile("(?<appname>.*)/(?<appversion>.*)\\((?<osname>.*);\\s(?<osversion>.*),\\s(?<device>.*)\\)");

    private static final String IOS = "ios";
    private static final String ANDROID = "android";

    private String appVersion;
    private String specialReleaseType;
    private String os;
    private String osVersion;

    public TinkUserAgent(String userAgentString) {
        if (!Strings.isNullOrEmpty(userAgentString)) {
            appVersion = parseAppVersion(userAgentString);
            specialReleaseType = parseSpecialReleaseType(userAgentString);
            os = parseOs(userAgentString);
            osVersion = parseOsVersion(userAgentString);
        }
    }

    public static TinkUserAgent of(Optional<String> userAgent) {
        return new TinkUserAgent(userAgent.orElse(null));
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getOs() {
        return os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getSpecialReleaseType() { 
    	return specialReleaseType; 
    }

    public boolean isBetaVersion() { 
    	return specialReleaseType != null && "beta".equals(specialReleaseType.toLowerCase());
    }

	/**
     * Check that the user agent's version is greater than given parameters
     * 
     * @param minIosVersion
     *            the minimum (inclusive) allowed iOS version. Can be null for no lower limit.
     * @param minAndroidVersion
     *            the minimum (inclusive) allowed Android version. Can be null for no lower limit.
     * @return true if the version is within the limits.
     */
    public boolean hasValidVersion(String minIosVersion, String minAndroidVersion) {
        return hasValidVersion(minIosVersion, null, minAndroidVersion, null);
    }

    public boolean hasValidIosAppVersion(String minIosVersion) {
        return hasValidVersion(minIosVersion, null);
    }

    /**
     * Check that the user agent's version is within certain range.
     * 
     * @param minIosVersion
     *            the minimum (inclusive) allowed iOS version. Can be null for no lower limit.
     * @param maxIosVersion
     *            the maximum (inclusive) allowed iOS version. Can be null for no upper limit.
     * @param minAndroidVersion
     *            the minimum (inclusive) allowed Android version. Can be null for no lower limit.
     * @param maxAndroidVersion
     *            the maximum (inclusive) allowed Android version. Can be null for no upper limit.
     * @return true if the version is within the limits.
     */
    public boolean hasValidVersion(String minIosVersion, String maxIosVersion, String minAndroidVersion,
            String maxAndroidVersion) {
        if (os == null || appVersion == null) {
            return true;
        }

        DefaultArtifactVersion minVersion = null;
        DefaultArtifactVersion maxVersion = null;

        if (Objects.equal(os, IOS)) {
            if (minIosVersion != null) {
                minVersion = new DefaultArtifactVersion(minIosVersion);
            }
            if (maxIosVersion != null) {
                maxVersion = new DefaultArtifactVersion(maxIosVersion);
            }
        }

        if (Objects.equal(os, ANDROID)) {
            if (minAndroidVersion != null) {
                minVersion = new DefaultArtifactVersion(minAndroidVersion);
            }
            if (maxAndroidVersion != null) {
                maxVersion = new DefaultArtifactVersion(maxAndroidVersion);
            }
        }

        if (minVersion == null && maxVersion == null) {
            return true;
        }

        // We have a version constraint (min, max or both) -- but we cannot retrieve app version
        if (appVersion == null) {
            return false;
        }

        DefaultArtifactVersion version = new DefaultArtifactVersion(appVersion);

        boolean isValidVersion = true;

        if (isValidVersion && minVersion != null) {
            isValidVersion = isValidVersion && version.compareTo(minVersion) >= 0;
        }

        if (isValidVersion && maxVersion != null) {
            isValidVersion = isValidVersion && version.compareTo(maxVersion) <= 0;
        }

        return isValidVersion;
    }

    private String parseAppVersion(String userAgentString) {
        Matcher clientMatcher = appVersionPattern.matcher(userAgentString);
        return clientMatcher.find() ? clientMatcher.group(1).replace("/", "") : null;
    }

    private String parseSpecialReleaseType(String userAgentString) {
        Matcher clientMatcher = appVersionPattern.matcher(userAgentString);
        return clientMatcher.find() ? clientMatcher.group(4) : null;
    }

    private String parseOs(String userAgentString) {
        if (userAgentString.toLowerCase().contains(IOS)) {
            return IOS;
        }
        if (userAgentString.toLowerCase().contains(ANDROID)) {
            return ANDROID;
        }
        return null;
    }

    private String parseOsVersion(String userAgentString) {
        Matcher matcher = userAgentPattern.matcher(userAgentString);
        return matcher.matches() ? matcher.group("osversion") : null;
    }

    public boolean hasMinimumOsVersion(String minOsVersion) {
        return new DefaultArtifactVersion(osVersion).compareTo(new DefaultArtifactVersion(minOsVersion)) >= 0;
    }

    public boolean isIOS() {
        return Objects.equal(IOS, os);
    }

    public boolean isAndroid() {
        return Objects.equal(ANDROID, os);
    }
}
