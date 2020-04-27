package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ListComparisonReporter;
import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.WrongBodyTypeComparisonReporter;
import se.tink.backend.aggregation.comparor.DifferenceEntity;

public class JSONListBodyEntity implements BodyEntity {

    private static ObjectMapper mapper = new ObjectMapper();

    private final ImmutableList<?> data;

    public JSONListBodyEntity(String rawData) {
        try {
            this.data = ImmutableList.copyOf(mapper.readValue(rawData, List.class));
        } catch (IOException e) {
            throw new RuntimeException("Could not create JSONListBodyEntity", e);
        }
    }

    @Override
    public ComparisonReporter compare(Object object) {
        if (object instanceof JSONListBodyEntity) {
            DifferenceEntity difference = null;
            difference = comparor.areListsMatching(this.data, ((JSONListBodyEntity) object).data);
            return new ListComparisonReporter(
                    this.data.size(), ((JSONListBodyEntity) object).data.size(), difference);
        }
        return new WrongBodyTypeComparisonReporter();
    }

    @Override
    public String getBodyType() {
        return "application/json";
    }
}
