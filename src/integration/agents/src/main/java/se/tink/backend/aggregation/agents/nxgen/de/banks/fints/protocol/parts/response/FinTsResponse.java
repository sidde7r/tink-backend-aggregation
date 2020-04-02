package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.FinTsParser;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@EqualsAndHashCode
public class FinTsResponse {

    private List<RawSegment> rawSegments;

    public FinTsResponse(String rawResponse) {
        this.rawSegments =
                FinTsParser.parse(rawResponse).stream()
                        .filter(RawSegment::isProperSegment)
                        .collect(Collectors.toList());

        if (hasEncryptionEnvelope()) {
            unpackEncryptionEnvelope();
        }
    }

    private boolean hasEncryptionEnvelope() {
        return findSegment(EncryptedEnvelope.class).isPresent();
    }

    private void unpackEncryptionEnvelope() {
        findSegment(EncryptedEnvelope.class)
                .ifPresent(
                        encryptedEnvelope ->
                                this.rawSegments.addAll(encryptedEnvelope.getRawSegments()));
    }

    public <T extends BaseResponsePart> Optional<T> findSegment(Class<T> type) {
        return findSegments(type).stream().findFirst();
    }

    public <T extends BaseResponsePart> T findSegmentThrowable(Class<T> type) {
        Optional<T> result = findSegments(type).stream().findFirst();
        return result.orElseThrow(
                () -> new IllegalArgumentException("Could not find segment: " + type));
    }

    public <T extends BaseResponsePart> List<T> findSegments(Class<T> type) {
        if (type == null || !SupportedResponseSegments.isSupported(type)) {
            throw new IllegalArgumentException(
                    "Provided type is not supported. Please add an entry in FinTsResponse class.");
        }

        SupportedResponseSegments.SegmentInfo segmentInformation =
                SupportedResponseSegments.getSegmentInformation(type);
        String lookingForName = segmentInformation.getSegmentType().getSegmentName();

        return rawSegments.stream()
                .filter(rawSegment -> lookingForName.equals(rawSegment.getSegmentName()))
                .map(rawSegment -> (T) segmentInformation.getConstructor().apply(rawSegment))
                .collect(Collectors.toList());
    }

    // Helper functions go here. Only things that are usable for a lot of responses should go here
    public boolean isSuccess() {
        return findSegments(MessageStatus.class).stream()
                .flatMap(messageStatus -> messageStatus.getResponses().stream())
                .map(SegmentStatus.Response::getResultCode)
                .noneMatch(resultCode -> resultCode.matches("^9.*"));
    }

    public boolean hasStatusCodeOf(String statusCode) {
        return Stream.concat(
                        findSegments(SegmentStatus.class).stream(),
                        findSegments(MessageStatus.class).stream())
                .anyMatch(status -> !status.getResponsesWithCode(statusCode).isEmpty());
    }

    public boolean hasAnyOfStatusCodes(String... statusCodes) {
        return Arrays.stream(statusCodes).anyMatch(this::hasStatusCodeOf);
    }
}
