package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.MapComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;
import se.tink.backend.aggregation.comparor.DifferenceEntity;

public class JSONMapBodyEntity implements BodyEntity {

    private static ObjectMapper mapper = new ObjectMapper();

    private final Map<String, Object> data;

    @SuppressWarnings("unchecked")
    JSONMapBodyEntity(String rawData) {
        try {
            data = Collections.unmodifiableMap(mapper.readValue(rawData, Map.class));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not create JSONMapBodyEntity", e);
        }
    }

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof JSONMapBodyEntity) {
            DifferenceEntity difference =
                    comparor.findDifferencesInMappings(
                            this.data, ((JSONMapBodyEntity) object).data);
            return new MapComparisonReporter(difference);
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "application/json";
    }
}
