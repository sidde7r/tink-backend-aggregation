package se.tink.backend.aggregation.configuration;

public interface ClientConfiguration {
    static String getDescriptionRedirectUrl() {
        return "From https://www.oauth.com/oauth2-servers/redirect-uris/: Redirect URLs are a\n"
                + "critical part of the OAuth flow. After a user successfully authorizes an\n"
                + "application, the authorization server will redirect the user back to the\n"
                + "application with either an authorization code or access token in the URL.\n"
                + "Because the redirect URL will contain sensitive information, it is critical\n"
                + "that the service doesn’t redirect the user to arbitrary locations.";
    }

    static String getExampleRedirectUrl() {
        return "https://api.tink.se/api/v1/credentials/third-party/callback";
    }

    static String getDescriptionClientId() {
        return "From https://www.oauth.com/oauth2-servers/client-registration/client-id-secret/: The\n"
                + "client_id is a public identifier for apps. Even though it’s public, it’s best that\n"
                + "it isn’t guessable by third parties, so many implementations use something like a\n"
                + "32-character hex string. It must also be unique across all clients that the\n"
                + "authorization server handles. If the client ID is guessable, it makes it slightly\n"
                + "easier to craft phishing attacks against arbitrary applications.";
    }

    static String getExampleClientId() {
        return "555d5513cb123456789050b90f06a18b21234567890a9cce977c5513734d3f41";
    }

    static String getDescriptionClientSecret() {
        return "From https://www.oauth.com/oauth2-servers/client-registration/client-id-secret/: The\n"
                + "client_secret is a secret known only to the application and the authorization server.\n"
                + "It must be sufficiently random to not be guessable, which means you should avoid\n"
                + "using common UUID libraries which often take into account the timestamp or MAC\n"
                + "address of the server generating it. A great way to generate a secure secret is to\n"
                + "use a cryptographically-secure library to generate a 256-bit value and converting it\n"
                + "to a hexadecimal representation.";
    }

    static String getExampleClientSecret() {
        return "042cfdf1234567890ad83d622d7563aa2412345678900d8fd891c490f183c6da";
    }
}
