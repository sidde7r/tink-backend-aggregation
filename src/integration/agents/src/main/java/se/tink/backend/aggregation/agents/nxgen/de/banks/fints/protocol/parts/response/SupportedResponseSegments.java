package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parser.RawSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public class SupportedResponseSegments {

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class SegmentInfo {
        private SegmentType segmentType;
        private Function<RawSegment, ? extends BaseResponsePart> constructor;
    }

    private static Map<Class<? extends BaseResponsePart>, SegmentInfo> knownSegmentsLookup =
            new HashMap<>();

    static {
        knownSegmentsLookup.put(
                TransactionsCamt.class, new SegmentInfo(SegmentType.HICAZ, TransactionsCamt::new));
        knownSegmentsLookup.put(
                TransactionsCamtBankConfig.class,
                new SegmentInfo(SegmentType.HICAZS, TransactionsCamtBankConfig::new));
        knownSegmentsLookup.put(
                TransactionsSwift.class,
                new SegmentInfo(SegmentType.HIKAZ, TransactionsSwift::new));
        knownSegmentsLookup.put(
                TransactionsSwiftBankConfig.class,
                new SegmentInfo(SegmentType.HIKAZS, TransactionsSwiftBankConfig::new));
        knownSegmentsLookup.put(
                TanInformation.class, new SegmentInfo(SegmentType.HIPINS, TanInformation::new));
        knownSegmentsLookup.put(
                MessageStatus.class, new SegmentInfo(SegmentType.HIRMG, MessageStatus::new));
        knownSegmentsLookup.put(
                SegmentStatus.class, new SegmentInfo(SegmentType.HIRMS, SegmentStatus::new));
        knownSegmentsLookup.put(Balance.class, new SegmentInfo(SegmentType.HISAL, Balance::new));
        knownSegmentsLookup.put(
                BalanceBankConfig.class,
                new SegmentInfo(SegmentType.HISALS, BalanceBankConfig::new));
        knownSegmentsLookup.put(
                SepaDetails.class, new SegmentInfo(SegmentType.HISPA, SepaDetails::new));
        knownSegmentsLookup.put(
                Synchronization.class, new SegmentInfo(SegmentType.HISYN, Synchronization::new));
        knownSegmentsLookup.put(
                TanMediaInformation.class,
                new SegmentInfo(SegmentType.HITAB, TanMediaInformation::new));
        knownSegmentsLookup.put(
                TanContext.class, new SegmentInfo(SegmentType.HITAN, TanContext::new));
        knownSegmentsLookup.put(
                BasicAccountInformation.class,
                new SegmentInfo(SegmentType.HIUPD, BasicAccountInformation::new));
        knownSegmentsLookup.put(Header.class, new SegmentInfo(SegmentType.HNHBK, Header::new));
        knownSegmentsLookup.put(
                EncryptedEnvelope.class,
                new SegmentInfo(SegmentType.HNVSD, EncryptedEnvelope::new));
    }

    static <T extends BaseResponsePart> boolean isSupported(Class<T> type) {
        return knownSegmentsLookup.containsKey(type);
    }

    static <T extends BaseResponsePart> SegmentInfo getSegmentInformation(Class<T> type) {
        return knownSegmentsLookup.get(type);
    }
}
