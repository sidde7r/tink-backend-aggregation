package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.detail;

import java.util.Arrays;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawGroup;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

public class RawSegmentComposer {

    public static RawSegment compose(String[][] data) {
        RawSegment segment = new RawSegment();
        for (String[] group : data) {
            segment.addGroup(new RawGroup(Arrays.asList(group)));
        }
        return segment;
    }
}
