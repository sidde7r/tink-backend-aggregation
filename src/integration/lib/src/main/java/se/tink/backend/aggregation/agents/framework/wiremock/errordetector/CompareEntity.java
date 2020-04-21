package se.tink.backend.aggregation.agents.framework.wiremock.errordetector;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

public class CompareEntity {

    public static class Builder {

        private final String givenRequest;
        private final String expectedRequest;
        private Boolean areUrlsMatching;
        private Boolean areHTTPMethodsMatching;
        private Boolean areBodyTypesMatching;
        private final Set<String> missingHeaderKeysInGivenRequest = new HashSet<>();
        private final Set<String> headerKeysWithDifferentValues = new HashSet<>();
        private final Set<String> missingBodyKeysInGivenRequest = new HashSet<>();
        private final Set<String> bodyKeysWithDifferentValues = new HashSet<>();

        public Builder(String givenRequest, String expectedRequest) {
            this.givenRequest = givenRequest;
            this.expectedRequest = expectedRequest;
        }

        public Builder setUrlsMatching(boolean value) {
            this.areUrlsMatching = value;
            return this;
        }

        public Builder setHTTPMethodMatching(boolean value) {
            this.areHTTPMethodsMatching = value;
            return this;
        }

        public Builder setBodyTypesMatching(boolean value) {
            this.areBodyTypesMatching = value;
            return this;
        }

        public Builder addMissingHeaderKeyInGivenRequest(String key) {
            this.missingHeaderKeysInGivenRequest.add(key);
            return this;
        }

        public Builder addMissingBodyKeyInGivenRequest(String key) {
            this.missingBodyKeysInGivenRequest.add(key);
            return this;
        }

        public Builder addHeaderKeyWithDifferentValue(String key) {
            this.headerKeysWithDifferentValues.add(key);
            return this;
        }

        public Builder addBodyKeyWithDifferentValue(String key) {
            this.bodyKeysWithDifferentValues.add(key);
            return this;
        }

        public CompareEntity build() {
            return new CompareEntity(
                    givenRequest,
                    expectedRequest,
                    areUrlsMatching,
                    areHTTPMethodsMatching,
                    areBodyTypesMatching,
                    missingHeaderKeysInGivenRequest,
                    headerKeysWithDifferentValues,
                    missingBodyKeysInGivenRequest,
                    bodyKeysWithDifferentValues);
        }
    }

    private CompareEntity(
            String givenRequest,
            String expectedRequest,
            boolean areUrlsMatching,
            boolean areMethodsMatching,
            boolean areBodyTypesMatching,
            Set<String> missingHeaderKeysInGivenRequest,
            Set<String> headerKeysWithDifferentValues,
            Set<String> missingBodyKeysInGivenRequest,
            Set<String> bodyKeysWithDifferentValues) {
        this.givenRequest = givenRequest;
        this.expectedRequest = expectedRequest;
        this.areUrlsMatching = areUrlsMatching;
        this.areMethodsMatching = areMethodsMatching;
        this.areBodyTypesMatching = areBodyTypesMatching;
        this.missingHeaderKeysInGivenRequest = missingHeaderKeysInGivenRequest;
        this.headerKeysWithDifferentValues = headerKeysWithDifferentValues;
        this.missingBodyKeysInGivenRequest = missingBodyKeysInGivenRequest;
        this.bodyKeysWithDifferentValues = bodyKeysWithDifferentValues;
    }

    private String givenRequest;
    private String expectedRequest;

    private boolean areUrlsMatching;
    private boolean areMethodsMatching;
    private boolean areBodyTypesMatching;

    private Set<String> missingHeaderKeysInGivenRequest;
    private Set<String> headerKeysWithDifferentValues;

    private Set<String> missingBodyKeysInGivenRequest;
    private Set<String> bodyKeysWithDifferentValues;

    public String getGivenRequest() {
        return givenRequest;
    }

    public String getExpectedRequest() {
        return expectedRequest;
    }

    public boolean areUrlsMatching() {
        return areUrlsMatching;
    }

    public boolean areMethodsMatching() {
        return areMethodsMatching;
    }

    public boolean areBodyTypesMatching() {
        return areBodyTypesMatching;
    }

    public Set<String> getMissingHeaderKeysInGivenRequest() {
        return ImmutableSet.copyOf(missingHeaderKeysInGivenRequest);
    }

    public Set<String> getHeaderKeysWithDifferentValues() {
        return ImmutableSet.copyOf(headerKeysWithDifferentValues);
    }

    public Set<String> getMissingBodyKeysInGivenRequest() {
        return ImmutableSet.copyOf(missingBodyKeysInGivenRequest);
    }

    public Set<String> getBodyKeysWithDifferentValues() {
        return ImmutableSet.copyOf(bodyKeysWithDifferentValues);
    }
}
