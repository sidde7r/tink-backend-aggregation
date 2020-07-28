package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.FinTsParser;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;

@EqualsAndHashCode
public class FinTsResponse {

    private static Map<
                    Class<? extends BaseResponsePart>,
                    Function<RawSegment, ? extends BaseResponsePart>>
            knownSegmentsLookup = new HashMap<>();

    static {
        knownSegmentsLookup.put(HICAZ.class, HICAZ::new);
        knownSegmentsLookup.put(HICAZS.class, HICAZS::new);
        knownSegmentsLookup.put(HIKAZ.class, HIKAZ::new);
        knownSegmentsLookup.put(HIKAZS.class, HIKAZS::new);
        knownSegmentsLookup.put(HIPINS.class, HIPINS::new);
        knownSegmentsLookup.put(HIRMG.class, HIRMG::new);
        knownSegmentsLookup.put(HIRMS.class, HIRMS::new);
        knownSegmentsLookup.put(HISAL.class, HISAL::new);
        knownSegmentsLookup.put(HISALS.class, HISALS::new);
        knownSegmentsLookup.put(HISPA.class, HISPA::new);
        knownSegmentsLookup.put(HISYN.class, HISYN::new);
        knownSegmentsLookup.put(HITAB.class, HITAB::new);
        knownSegmentsLookup.put(HITAN.class, HITAN::new);
        knownSegmentsLookup.put(HIUPD.class, HIUPD::new);
        knownSegmentsLookup.put(HNHBK.class, HNHBK::new);
        knownSegmentsLookup.put(HNVSD.class, HNVSD::new);
        knownSegmentsLookup.put(HITANS.class, HITANS::new);
    }

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
        return findSegment(HNVSD.class).isPresent();
    }

    private void unpackEncryptionEnvelope() {
        findSegment(HNVSD.class)
                .ifPresent(hnvsd -> this.rawSegments.addAll(hnvsd.getRawSegments()));
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
        if (type == null) {
            return Collections.emptyList();
        }

        String lookingForName = type.getSimpleName();
        Function<RawSegment, ? extends BaseResponsePart> constructor =
                knownSegmentsLookup.get(type);
        return rawSegments.stream()
                .filter(x -> lookingForName.equals(x.getSegmentName()))
                .map(rawSegment -> (T) constructor.apply(rawSegment))
                .collect(Collectors.toList());
    }

    public <T extends BaseResponsePart> Optional<T> findSegmentWithSupportedVersions(
            Class<T> type) {
        return findSegments(type).stream()
                .filter(a -> a.getSupportedVersions().contains(a.segmentVersion))
                .findFirst();
    }

    // Helper functions go here. Only things that are usable for a lot of responses should go here
    public boolean isSuccess() {
        return findSegments(HIRMG.class).stream()
                .flatMap(hirmg -> hirmg.getResponses().stream())
                .map(HIRMS.Response::getResultCode)
                .noneMatch(resultCode -> resultCode.matches("^9.*"));
    }

    public boolean hasStatusCodeOf(String statusCode) {
        return Stream.concat(findSegments(HIRMS.class).stream(), findSegments(HIRMG.class).stream())
                .anyMatch(hirmx -> !hirmx.getResponsesWithCode(statusCode).isEmpty());
    }

    public boolean hasStatusMessageOf(String message) {
        return Stream.concat(findSegments(HIRMS.class).stream(), findSegments(HIRMG.class).stream())
                .anyMatch(hirmx -> !hirmx.getResponsesWithMessage(message).isEmpty());
    }

    public boolean hasAnyOfStatusCodes(String... statusCodes) {
        return Arrays.stream(statusCodes).anyMatch(this::hasStatusCodeOf);
    }

    public boolean hasAnyStatusMessageOf(String... messages) {
        return Arrays.stream(messages).anyMatch(this::hasStatusMessageOf);
    }
}
