package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import lombok.RequiredArgsConstructor;
import se.tink.libraries.ab_test_group_calculation.ABTestingGroupCalculator;

@RequiredArgsConstructor
// TODO (AAP-1301): Find a better place for this class
public class AbTestingFlagSupplier {

    private static final ABTestingGroupCalculator GROUP_CALCULATOR =
            ABTestingGroupCalculator.newMd5GroupCalculator();
    private final double testGroupLimit;

    public Boolean get(String key) {
        if (testGroupLimit >= 1.0) {
            return true;
        }
        return GROUP_CALCULATOR.isInTestGroup(testGroupLimit, key);
    }
}
