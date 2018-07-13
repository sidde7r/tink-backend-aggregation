package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuItem {
    private String name;
    private String uri;
    private Method method;
    private Authorization authorization;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public enum Method {
        GET, POST, PUT, DELETE;

        @JsonCreator
        public static Method forValue(String methodName) {
            try {
                return valueOf(methodName);
            } catch (IllegalArgumentException notFound) {
                return null;
            }
        }

        @JsonValue
        public String toValue() {
            return name();
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public boolean isAuthorizedURI(MenuItem.Method requestMethodToCheck) {
        return Objects.equal(method, requestMethodToCheck) &&
                Objects.equal(authorization, Authorization.AUTHORIZED);
    }

    public enum Authorization {
        AUTHORIZED, REQUIRES_AUTH_METHOD_CHANGE, UNAUTHORIZED;

        @JsonCreator
        public static Authorization forValue(String authorizationName) {
            try {
                return valueOf(authorizationName);
            } catch (IllegalArgumentException notFound) {
                return null;
            }
        }

        @JsonValue
        public String toValue() {
            return name();
        }

        @Override
        public String toString() {
            return name();
        }
    }
}
