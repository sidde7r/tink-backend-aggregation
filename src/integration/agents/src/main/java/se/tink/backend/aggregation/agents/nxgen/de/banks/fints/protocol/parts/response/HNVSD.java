package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.FinTsParser;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class HNVSD extends BaseResponsePart {
    private List<RawSegment> rawSegments;

    HNVSD(RawSegment rawSegment) {
        super(rawSegment);
        rawSegments =
                FinTsParser.parse(rawSegment.getGroup(1).getString(0)).stream()
                        .filter(RawSegment::isProperSegment)
                        .collect(Collectors.toList());
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(1);
    }
}
