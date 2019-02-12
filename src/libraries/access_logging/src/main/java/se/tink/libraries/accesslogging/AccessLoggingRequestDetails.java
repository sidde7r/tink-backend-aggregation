package se.tink.libraries.accesslogging;

import se.tink.libraries.auth.HttpAuthenticationMethod;

public class AccessLoggingRequestDetails {
    private String remoteHost;
    private String requestString;
    private String responseStatus;
    private String userAgent;
    private String responseTimeString;
    private String userId;
    private HttpAuthenticationMethod httpAuthenticationMethod;
    private String sessionId;
    private String body;
    private String oauthClientId;

    private AccessLoggingRequestDetails(String remoteHost, String requestString, String responseStatus,
            String userAgent, String responseTimeString, String userId,
            HttpAuthenticationMethod httpAuthenticationMethod, String sessionId, String body, String oauthClientId) {
        this.remoteHost = remoteHost;
        this.requestString = requestString;
        this.responseStatus = responseStatus;
        this.userAgent = userAgent;
        this.responseTimeString = responseTimeString;
        this.userId = userId;
        this.httpAuthenticationMethod = httpAuthenticationMethod;
        this.sessionId = sessionId;
        this.body = body;
        this.oauthClientId = oauthClientId;
    }

    public static AccessLoggingCommandBuilder builder() {
        return new AccessLoggingCommandBuilder();
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public String getRequestString() {
        return requestString;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getResponseTimeString() {
        return responseTimeString;
    }

    public String getUserId() {
        return userId;
    }

    public HttpAuthenticationMethod getHttpAuthenticationMethod() {
        return httpAuthenticationMethod;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getBody() {
        return body;
    }

    public String getOauthClientId() {
        return oauthClientId;
    }

    public static class AccessLoggingCommandBuilder {
        private String remoteHost;
        private String requestString;
        private String responseStatus;
        private String userAgent;
        private String responseTimeString;
        private String userId;
        private HttpAuthenticationMethod httpAuthenticationMethod;
        private String sessionId;
        private String body;
        private String oauthClientId;

        public AccessLoggingCommandBuilder setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public AccessLoggingCommandBuilder setRequestString(String requestString) {
            this.requestString = requestString;
            return this;
        }

        public AccessLoggingCommandBuilder setResponseStatus(String responseStatus) {
            this.responseStatus = responseStatus;
            return this;
        }

        public AccessLoggingCommandBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AccessLoggingCommandBuilder setResponseTimeString(String responseTimeString) {
            this.responseTimeString = responseTimeString;
            return this;
        }

        public AccessLoggingCommandBuilder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public AccessLoggingCommandBuilder setHttpAuthenticationMethod(HttpAuthenticationMethod httpAuthenticationMethod) {
            this.httpAuthenticationMethod = httpAuthenticationMethod;
            return this;
        }

        public AccessLoggingCommandBuilder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public AccessLoggingCommandBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public AccessLoggingCommandBuilder setOauthClientId(String oauthClientId) {
            this.oauthClientId = oauthClientId;
            return this;
        }

        public AccessLoggingRequestDetails build() {
            return new AccessLoggingRequestDetails(remoteHost, requestString, responseStatus, userAgent,
                    responseTimeString, userId, httpAuthenticationMethod, sessionId, body, oauthClientId);
        }
    }
}
