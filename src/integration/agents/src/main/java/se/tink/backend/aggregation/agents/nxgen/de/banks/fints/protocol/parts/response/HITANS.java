package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Slf4j
@Getter
public class HITANS extends BaseResponsePart {

    private final Map<String, String> allowedScaMethods;
    private static final ImmutableMap<Integer, FieldPositions> versionPositionsMapping;

    static {
        versionPositionsMapping =
                ImmutableMap.<Integer, FieldPositions>builder()
                        .put(6, new FieldPositions(21, 3, 5))
                        .put(5, new FieldPositions(22, 3, 5))
                        .put(4, new FieldPositions(22, 3, 5))
                        .put(3, new FieldPositions(18, 3, 3))
                        .put(2, new FieldPositions(15, 3, 3))
                        .put(1, new FieldPositions(11, 4, 3))
                        .build();
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return ImmutableList.of(1, 2, 3, 4, 5, 6);
    }

    HITANS(RawSegment rawSegment) {
        super(rawSegment);
        RawGroup allowedScaMethodsGroup = rawSegment.getGroup(4);
        this.allowedScaMethods = setupAllowedScaMethods(allowedScaMethodsGroup);
    }

    private Map<String, String> setupAllowedScaMethods(RawGroup allowedScaMethodsGroup) {
        Map<String, String> result = new HashMap<>();
        // HITANS segment contains many irrelevant fields, we need only auth code and name which
        // position is version specific.
        FieldPositions fieldPositions = versionPositionsMapping.get(this.segmentVersion);

        for (int i = fieldPositions.authMethodsBlocksStartPosition;
                i < allowedScaMethodsGroup.size();
                i += fieldPositions.authMethodsBlockSize) {
            result.put(
                    allowedScaMethodsGroup.getString(i),
                    allowedScaMethodsGroup.getString(i + fieldPositions.authNamePositionInBlock));
        }
        return result;
    }

    @AllArgsConstructor
    static class FieldPositions {
        int authMethodsBlockSize;
        int authMethodsBlocksStartPosition;
        int authNamePositionInBlock;
    }
}
