package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.libraries.metrics.core.MetricId;

@Getter
public enum NemIdMetricLabel {
    WEB_DRIVER_CONSTRUCTION("web_driver_construction"),
    NEM_ID_IFRAME_AUTH_METRIC(
            "nem_id_iframe_auth", bucketsSequence(30, 6 * Constants.SECONDS_IN_MINUTE, 30)),
    FETCHING_NEM_ID_PARAMETERS(
            "fetching_nem_id_parameters", bucketsSequence(15, 4 * Constants.SECONDS_IN_MINUTE, 15)),
    WAITING_FOR_NEM_ID_IFRAME_METRIC(
            "waiting_for_nem_id_iframe", buckets(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 30, 45, 60)),
    WAITING_FOR_USER_INPUT_METRIC(
            "waiting_for_user_input", buckets(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 30, 45, 60)),
    WAITING_FOR_CREDENTIALS_VALIDATION_ELEMENTS_METRIC(
            "waiting_for_credentials_validation_elements",
            buckets(1, 2, 3, 4, 5, 10, 15, 20, 25, 30)),
    WAITING_FOR_SUPPLEMENTAL_INFO_METRIC(
            "waiting_for_supplemental_info",
            bucketsSequence(30, NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN, 30)),
    WAITING_FOR_TOKEN_METRIC(
            "waiting_for_token",
            bucketsSequence(30, NemIdConstants.NEM_ID_TIMEOUT_SECONDS_WITH_SAFETY_MARGIN, 30));

    private final String name;
    private final List<Number> customBuckets;
    private final MetricId metricId;

    NemIdMetricLabel(String name) {
        this(name, Collections.emptyList());
    }

    NemIdMetricLabel(String name, List<Number> customBuckets) {
        this.name = name;
        this.customBuckets = customBuckets;
        this.metricId =
                Constants.NEM_ID_AUTHENTICATION_METRIC_ID.label(
                        Constants.NEM_ID_AUTHENTICATION_PHASE_LABEL, name);
    }

    private static List<Number> buckets(Number... bucketValues) {
        return Stream.of(bucketValues).collect(Collectors.toList());
    }

    @SuppressWarnings("SameParameterValue")
    private static List<Number> bucketsSequence(int start, int end, int step) {
        List<Number> buckets = new ArrayList<>();

        int current = start;
        while (current <= end) {
            buckets.add(current);
            current += step;
        }

        return buckets;
    }

    private static class Constants {
        public static final int SECONDS_IN_MINUTE = 60;

        public static final MetricId NEM_ID_AUTHENTICATION_METRIC_ID =
                MetricId.newId("nem_id_iframe_auth_seconds");
        public static final String NEM_ID_AUTHENTICATION_PHASE_LABEL = "phase";
    }
}
