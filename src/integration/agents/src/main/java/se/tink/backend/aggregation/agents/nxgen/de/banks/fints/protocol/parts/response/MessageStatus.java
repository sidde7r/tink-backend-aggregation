package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class MessageStatus extends SegmentStatus {
    MessageStatus(RawSegment rawSegment) {
        super(rawSegment);
    }
}
