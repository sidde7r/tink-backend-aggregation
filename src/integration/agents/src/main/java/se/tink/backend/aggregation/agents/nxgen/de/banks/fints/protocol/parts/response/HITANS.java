package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Slf4j
@Getter
public class HITANS extends BaseResponsePart {

    private final Map<String, String> allowedScaMethods;

    @Override
    protected List<Integer> getSupportedVersions() {
        return ImmutableList.of(6);
    }

    HITANS(RawSegment rawSegment) {
        super(rawSegment);
        RawGroup allowedScaMethodsGroup = rawSegment.getGroup(4);
        this.allowedScaMethods = setupAllowedScaMethods(allowedScaMethodsGroup);
    }

    private static Map<String, String> setupAllowedScaMethods(RawGroup allowedScaMethodsGroup) {
        Map<String, String> result = new HashMap<>();
        // HITANS segment contains 3 technical fields which we can ignore and 21 fields per
        // authentication method
        // we only need code and name (1st and 6th element of each entry)
        for (int i = 3; i < allowedScaMethodsGroup.size(); i += 21) {
            result.put(
                    allowedScaMethodsGroup.getString(i), allowedScaMethodsGroup.getString(i + 5));
        }
        return result;
    }
}
