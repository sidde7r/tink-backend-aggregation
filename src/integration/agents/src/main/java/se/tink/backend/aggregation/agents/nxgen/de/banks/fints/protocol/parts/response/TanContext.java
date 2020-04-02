package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class TanContext extends BaseResponsePart {

    private String tanProcess;
    private String taskHashValue;
    private String taskReference; // if SCA is not needed then bank should return "noref"
    private String challenge; // if SCA is not needed then bank should return "nochallenge"
    private String challengeHhduc;
    private String challengeValidUntil;
    private String tanMediumName;

    TanContext(RawSegment rawSegment) {
        super(rawSegment);
        tanProcess = rawSegment.getGroup(1).getString(0);
        taskHashValue = rawSegment.getGroup(2).getString(0);
        taskReference = rawSegment.getGroup(3).getString(0);
        challenge = rawSegment.getGroup(4).getString(0);
        challengeHhduc = rawSegment.getGroup(5).getString(0);
        challengeValidUntil = rawSegment.getGroup(6).getString(0);
        tanMediumName = rawSegment.getGroup(7).getString(0);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(6);
    }
}
