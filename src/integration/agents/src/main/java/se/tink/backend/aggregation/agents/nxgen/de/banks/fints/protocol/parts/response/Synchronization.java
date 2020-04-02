package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class Synchronization extends BaseResponsePart {

    private String systemId;

    Synchronization(RawSegment rawSegment) {
        super(rawSegment);
        this.systemId = rawSegment.getGroup(1).getString(0);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(4);
    }
}
