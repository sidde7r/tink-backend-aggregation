package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SupportedRequestSegments {

    public static final Map<SegmentType, List<Integer>> supportedVersions = new HashMap<>();

    static {
        supportedVersions.put(SegmentType.HKKAZ, Arrays.asList(5));
        supportedVersions.put(SegmentType.HKCAZ, Arrays.asList(1));
        supportedVersions.put(SegmentType.HKSAL, Arrays.asList(5, 6, 7));
        supportedVersions.put(SegmentType.HKWPD, Arrays.asList(5, 6));
    }

    public static OptionalInt getHighestCommonVersion(
            FinTsDialogContext dialogContext, SegmentType segmentType) {
        return pickLargestCommon(
                dialogContext.getVersionsOfOperationSupportedByBank(segmentType),
                supportedVersions.get(segmentType));
    }

    public static int getHighestCommonVersionThrowable(
            FinTsDialogContext dialogContext, SegmentType segmentType) {
        OptionalInt commonVersion =
                pickLargestCommon(
                        dialogContext.getVersionsOfOperationSupportedByBank(segmentType),
                        supportedVersions.get(segmentType));
        return commonVersion.orElseThrow(
                () ->
                        new IllegalArgumentException(
                                "Could not find common version for: " + segmentType));
    }

    private static OptionalInt pickLargestCommon(
            List<Integer> supportedByThem, List<Integer> supportedByUs) {
        supportedByThem.retainAll(supportedByUs);
        return supportedByThem.stream().mapToInt(x -> x).max();
    }
}
