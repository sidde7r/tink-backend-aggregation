package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import lombok.RequiredArgsConstructor;
import se.tink.libraries.ab_test_group_calculation.ABTestingGroupCalculator;

@RequiredArgsConstructor
public class AbTestingFlagSupplier {

    private static final ABTestingGroupCalculator GROUP_CALCULATOR =
            ABTestingGroupCalculator.newMd5GroupCalculator();
    private final double testGroupLimit;

    public Boolean get(String key) {
        return GROUP_CALCULATOR.isInTestGroup(testGroupLimit, key);
    }
}
