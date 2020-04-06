package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public abstract class BaseResponsePart {

    protected String segmentName;
    protected int segmentVersion;
    protected int segmentPosition;
    protected Integer referencedSegmentPosition;

    BaseResponsePart(RawSegment rawSegment) {
        RawGroup headerGroup = rawSegment.getGroup(0);
        segmentName = headerGroup.getString(0);
        segmentPosition = headerGroup.getInteger(1);
        segmentVersion = headerGroup.getInteger(2);
        referencedSegmentPosition = headerGroup.getInteger(3);

        validateSegmentVersion();
    }

    protected abstract List<Integer> getSupportedVersions();

    private void validateSegmentVersion() {
        if (!getSupportedVersions().contains(segmentVersion)) {
            throw new IllegalArgumentException(
                    String.format("Unsupported %s version %s", segmentName, segmentVersion));
        }
    }
}
