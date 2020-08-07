package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.MapComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;
import se.tink.backend.aggregation.comparor.DifferenceEntity;

public class URLEncodedBodyEntity implements BodyEntity {

    private final ImmutableMap<String, Object> data;

    public URLEncodedBodyEntity(String rawData) {
        Map<String, Object> body = new HashMap<>();

        Arrays.asList(rawData.split("&"))
                .forEach(
                        element -> {
                            final String[] elements = element.split("=");
                            final String key = elements[0];
                            final String value = (elements.length > 1) ? elements[1] : "";

                            body.put(key, value);
                        });

        this.data = ImmutableMap.copyOf(body);
    }

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof URLEncodedBodyEntity) {
            DifferenceEntity difference = null;
            difference =
                    comparor.findDifferencesInMappings(
                            this.data, ((URLEncodedBodyEntity) object).data);
            return new MapComparisonReporter(difference);
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "application/x-www-form-urlencoded";
    }
}
