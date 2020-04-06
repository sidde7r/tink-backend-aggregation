package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.FinTsParser;

/**
 * https://www.hbci-zka.de/dokumente/spezifikation_deutsch/fintsv3/FinTS_3.0_Security_Sicherheitsverfahren_HBCI_Rel_20181129_final_version.pdf
 * Page 46
 */
public class HNVSDv1 extends BaseRequestPart {

    private List<BaseRequestPart> segments = new ArrayList<>();

    public void addSegment(BaseRequestPart subSegment) {
        segments.add(subSegment);
    }

    @Override
    protected SegmentPositionCounter assignSegmentPosition(SegmentPositionCounter counter) {
        segmentPosition = 999;
        for (BaseRequestPart segment : segments) {
            counter = segment.assignSegmentPosition(counter);
        }
        return counter;
    }

    @Override
    protected void compile() {
        super.compile();
        String innerContent =
                segments.size() == 0
                        ? ""
                        : segments.stream()
                                .map(BaseRequestPart::toFinTsFormat)
                                .collect(
                                        Collectors.joining(
                                                FinTsParser.SEGMENT_DELIMITER,
                                                "",
                                                FinTsParser.SEGMENT_DELIMITER));

        addGroup().element(innerContent.getBytes());
    }
}
