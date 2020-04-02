package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@Getter
public class SegmentStatus extends BaseResponsePart {

    @Getter
    @AllArgsConstructor
    public static class Response {
        private String resultCode;
        private String referenceElement;
        private String text;
        private List<String> parameters;
    }

    private List<Response> responses;

    SegmentStatus(RawSegment rawSegment) {
        super(rawSegment);
        responses =
                rawSegment.getGroups().stream()
                        .skip(1)
                        .map(
                                group ->
                                        new Response(
                                                group.getString(0),
                                                group.getString(1),
                                                group.getString(2),
                                                group.slice(3, group.size())))
                        .collect(Collectors.toList());
    }

    @Override
    protected List<Integer> getSupportedVersions() {
        return Collections.singletonList(2);
    }

    public List<Response> getResponsesWithCode(String code) {
        return responses.stream()
                .filter(r -> code.equals(r.getResultCode()))
                .collect(Collectors.toList());
    }
}
