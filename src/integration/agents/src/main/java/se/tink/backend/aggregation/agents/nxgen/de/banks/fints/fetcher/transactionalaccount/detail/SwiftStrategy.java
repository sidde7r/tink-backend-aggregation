package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKKAZv5;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIKAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class SwiftStrategy extends FinTsTransactionFetchingStrategy {

    public SwiftStrategy(
            TransactionClient transactionClient, FinTsTransactionMapper mapper, int version) {
        super(transactionClient, mapper, version);
    }

    @Override
    public List<AggregationTransaction> execute(FinTsAccountInformation account) {
        return transactionClient.getTransactionResponses(pickBuilder(), account).stream()
                .flatMap(x -> x.findSegments(HIKAZ.class).stream())
                .flatMap(hikaz -> Stream.of(hikaz.getBooked(), hikaz.getNotBooked()))
                .filter(Objects::nonNull)
                .flatMap(x -> mapper.parseSwift(x).stream())
                .collect(Collectors.toList());
    }

    TransactionRequestBuilder pickBuilder() {
        if (version != 5) {
            throw new IllegalArgumentException("Unsupported SwiftStrategy version: " + version);
        } else {
            return SWIFT_5;
        }
    }

    private static TransactionRequestBuilder SWIFT_5 =
            (dialogContext, account, startingPoint) -> {
                List<BaseRequestPart> additionalSegments = new ArrayList<>();
                additionalSegments.add(
                        HKKAZv5.builder()
                                .accountNumber(account.getBasicInfo().getAccountNumber())
                                .subAccountNumber(account.getBasicInfo().getSubAccountNumber())
                                .blz(account.getBasicInfo().getBlz())
                                .startingPoint(startingPoint)
                                .build());
                if (dialogContext.doesOperationRequireTAN(SegmentType.HKKAZ)) {
                    additionalSegments.add(
                            HKTANv6.builder()
                                    .tanProcessVariant(
                                            HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                                    .segmentType(SegmentType.HKKAZ)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }

                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
}
