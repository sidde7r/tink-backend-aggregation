package se.tink.backend.aggregation.agents.framework.wiremock.errordetector;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;

public class CompareEntity {

    public static class Builder {

        private final String givenRequest;
        private final String expectedRequest;
        private Boolean areUrlsMatching;
        private Boolean areHTTPMethodsMatching;
        private final Set<String> missingHeaderKeysInGivenRequest = new HashSet<>();
        private final Set<String> headerKeysWithDifferentValues = new HashSet<>();
        private ComparisonReporter bodyComparisonReporter;

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

        public Builder addMissingHeaderKeyInGivenRequest(String key) {
            this.missingHeaderKeysInGivenRequest.add(key);
            return this;
        }

        public Builder addHeaderKeyWithDifferentValue(String key) {
            this.headerKeysWithDifferentValues.add(key);
            return this;
        }

        public Builder addBodyComparisonReporter(ComparisonReporter reporter) {
            this.bodyComparisonReporter = reporter;
            return this;
        }

        public CompareEntity build() {
            return new CompareEntity(
                    givenRequest,
                    expectedRequest,
                    areUrlsMatching,
                    areHTTPMethodsMatching,
                    missingHeaderKeysInGivenRequest,
                    headerKeysWithDifferentValues,
                    bodyComparisonReporter);
        }
    }

    private CompareEntity(
            String givenRequest,
            String expectedRequest,
            boolean areUrlsMatching,
            boolean areMethodsMatching,
            Set<String> missingHeaderKeysInGivenRequest,
            Set<String> headerKeysWithDifferentValues,
            ComparisonReporter reporter) {
        this.givenRequest = givenRequest;
        this.expectedRequest = expectedRequest;
        this.areUrlsMatching = areUrlsMatching;
        this.areMethodsMatching = areMethodsMatching;
        this.missingHeaderKeysInGivenRequest = missingHeaderKeysInGivenRequest;
        this.headerKeysWithDifferentValues = headerKeysWithDifferentValues;
        this.bodyComparisonReporter = reporter;
    }

    private String givenRequest;
    private String expectedRequest;

    private boolean areUrlsMatching;
    private boolean areMethodsMatching;
    private boolean areBodyTypesMatching;

    private Set<String> missingHeaderKeysInGivenRequest;
    private Set<String> headerKeysWithDifferentValues;

    private ComparisonReporter bodyComparisonReporter;

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

    public ComparisonReporter getBodyComparisonReporter() {
        return bodyComparisonReporter;
    }
}
