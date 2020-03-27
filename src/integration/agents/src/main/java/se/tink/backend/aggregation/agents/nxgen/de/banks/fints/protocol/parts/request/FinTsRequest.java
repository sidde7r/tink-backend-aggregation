package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request;

import static se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType.HNHBK;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.FinTsParser;

public class FinTsRequest {

    // Header segment is an exception of a rule. It needs a bit of re-processing after all other
    // segments in a request have been processed.
    // Ie. it needs to know the length of an entire message.
    // This is why this class needs to know if it should launch a specific logic for that segment.
    // One could argue that every message has a header, but I want to keep it as 'generic' as
    // possible.
    private boolean hasHeaderSegment;
    private List<BaseRequestPart> segments;

    public FinTsRequest() {
        this.segments = new ArrayList<>();
    }

    public void addSegment(BaseRequestPart segment) {
        if (HNHBK.getSegmentName().equals(segment.getSegmentName())) {
            hasHeaderSegment = true;
        }
        segments.add(segment);
    }

    public String toFinTsFormat() {
        SegmentPositionCounter counter = new SegmentPositionCounter();

        for (BaseRequestPart segment : segments) {
            segment.assignSegmentPosition(counter);
        }

        String requestString =
                segments.stream()
                        .map(BaseRequestPart::toFinTsFormat)
                        .collect(
                                Collectors.joining(
                                        FinTsParser.SEGMENT_DELIMITER,
                                        "",
                                        FinTsParser.SEGMENT_DELIMITER));
        if (hasHeaderSegment) {
            requestString =
                    requestString.replaceFirst(
                            formatLength(0), formatLength(requestString.length()));
        }
        return requestString;
    }

    private String formatLength(int length) {
        return String.format("%012d", length);
    }

    public static FinTsRequest createEncryptedRequest(
            FinTsDialogContext context, List<BaseRequestPart> additionalSegments) {
        FinTsRequest request = new FinTsRequest();
        request.addSegment(
                HNHBKv3.builder()
                        .dialogId(context.getDialogId())
                        .messageNumber(context.getMessageNumber())
                        .build());
        request.addSegment(
                HNVSKv3.builder()
                        .securityProcedureVersion(context.getSecurityProcedureVersion())
                        .systemId(context.getSystemId())
                        .blz(context.getConfiguration().getBlz())
                        .username(context.getConfiguration().getUsername())
                        .build());
        HNVSDv1 hnvsd = new HNVSDv1();
        hnvsd.addSegment(
                HNSHKv4.builder()
                        .securityProcedureVersion(context.getSecurityProcedureVersion())
                        .securityFunction(context.getChosenSecurityFunction())
                        .securityReference(context.getSecurityReference())
                        .systemId(context.getSystemId())
                        .blz(context.getConfiguration().getBlz())
                        .username(context.getConfiguration().getUsername())
                        .build());

        additionalSegments.forEach(hnvsd::addSegment);

        hnvsd.addSegment(
                HNSHAv2.builder()
                        .securityReference(context.getSecurityReference())
                        .password(context.getConfiguration().getPassword())
                        .tanAnswer(context.getTanAnswer())
                        .build());
        request.addSegment(hnvsd);
        request.addSegment(HNHBSv1.builder().messageNumber(context.getMessageNumber()).build());
        return request;
    }
}
