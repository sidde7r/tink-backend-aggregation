package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class Header extends BaseResponsePart {
    private String dialogId;

    Header(RawSegment rawSegment) {
        super(rawSegment);
        this.dialogId = rawSegment.getGroup(3).getString(0);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(3);
    }
}
