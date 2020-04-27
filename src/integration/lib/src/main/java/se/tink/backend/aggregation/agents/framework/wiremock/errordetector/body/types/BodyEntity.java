package se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.types;

import se.tink.backend.aggregation.agents.framework.wiremock.errordetector.body.comparison.ComparisonReporter;
import se.tink.backend.aggregation.comparor.Comparor;
import se.tink.backend.aggregation.comparor.DifferenceCounter;
import se.tink.backend.aggregation.comparor.MapDifferenceEntity;

public interface BodyEntity {

    static final Comparor comparor =
            new Comparor(
                    new DifferenceCounter() {
                        @Override
                        public int numberOfDifferences(MapDifferenceEntity allDifferences) {
                            return allDifferences.getEntriesOnlyOnExpected().size()
                                    + allDifferences.getDifferenceInCommonKeys().size();
                        }
                    });

    ComparisonReporter compare(Object object);

    String getBodyType();
}
