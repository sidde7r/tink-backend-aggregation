package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public class SupportedRequestSegments {

    public static final Map<SegmentType, List<Integer>> supportedVersions = new HashMap<>();

    static {
        supportedVersions.put(SegmentType.HKKAZ, Arrays.asList(5));
        supportedVersions.put(SegmentType.HKCAZ, Arrays.asList(1));
        supportedVersions.put(SegmentType.HKSAL, Arrays.asList(5, 6, 7));
    }

    public static OptionalInt getHighestCommonVersion(
            FinTsDialogContext dialogContext, SegmentType segmentType) {
        return pickLargestCommon(
                dialogContext.getVersionsOfOperationSupportedByBank(segmentType),
                supportedVersions.get(segmentType));
    }

    private static OptionalInt pickLargestCommon(
            List<Integer> supportedByThem, List<Integer> supportedByUs) {
        supportedByThem.retainAll(supportedByUs);
        return supportedByThem.stream().mapToInt(x -> x).max();
    }
}
