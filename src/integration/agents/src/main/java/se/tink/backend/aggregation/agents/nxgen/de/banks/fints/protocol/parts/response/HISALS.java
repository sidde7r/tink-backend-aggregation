package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Accessors(chain = true)
@NoArgsConstructor
@Getter
@Setter
public class HISALS extends BaseResponsePart {
    HISALS(RawSegment rawSegment) {
        super(rawSegment);
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Arrays.asList(3, 4, 5, 6, 7);
    }
}
