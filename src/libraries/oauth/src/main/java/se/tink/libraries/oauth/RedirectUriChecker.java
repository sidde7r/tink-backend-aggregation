package se.tink.libraries.oauth;

        import java.net.URI;
        import java.net.URISyntaxException;
        import java.util.Objects;

/**
 * Helper class to validate redirect uris.
 */
public class RedirectUriChecker {

    public static boolean validate(String redirectUri) {
        try {
            URI uri = new URI(redirectUri);
            String scheme = uri.getScheme().toLowerCase();

            switch (scheme) {
            case "http":
                return Objects.equals(uri.getHost(), "localhost");
            default:
                // The reason for returning true here is so that we can allow:
                // 1. URIs with a https scheme
                // 2. Custom schemes for mobile apps (myapp://tink/callback)
                return true;
            }

        } catch (URISyntaxException e) {
            return false;
        }
    }
}
