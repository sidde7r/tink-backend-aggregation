package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.detail;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction.TransactionRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKCAZv1;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;

public class CamtStrategy extends FinTsTransactionFetchingStrategy {

    public CamtStrategy(
            TransactionClient transactionClient, FinTsTransactionMapper mapper, int version) {
        super(transactionClient, mapper, version);
    }

    @Override
    public List<AggregationTransaction> execute(FinTsAccountInformation account) {
        return transactionClient.getTransactionResponses(pickBuilder(), account).stream()
                .flatMap(x -> x.findSegments(HICAZ.class).stream())
                .flatMap(hicaz -> hicaz.getCamtFiles().stream())
                .flatMap(x -> mapper.parseCamt(x).stream())
                .collect(Collectors.toList());
    }

    TransactionRequestBuilder pickBuilder() {
        if (version != 1) {
            throw new IllegalArgumentException("Unsupported CamtStrategy version: " + version);
        } else {
            return CAMT_1;
        }
    }

    private static TransactionRequestBuilder CAMT_1 =
            (dialogContext, account, startingPoint) -> {
                HICAZS hicazs =
                        (HICAZS) dialogContext.getDetailsOfSupportedOperation(SegmentType.HKCAZ, 1);

                List<BaseRequestPart> additionalSegments = new ArrayList<>();
                HKCAZv1.HKCAZv1Builder hkcazBuilder =
                        HKCAZv1.builder()
                                .accountNumber(account.getBasicInfo().getAccountNumber())
                                .subAccountNumber(account.getBasicInfo().getSubAccountNumber())
                                .blz(account.getBasicInfo().getBlz())
                                .camtFormat(hicazs.getSupportedCamtFormats().get(0))
                                .startingPoint(startingPoint);

                if (account.getSepaDetails() != null) {
                    hkcazBuilder
                            .iban(account.getSepaDetails().getIban())
                            .bic(account.getSepaDetails().getBic());
                }

                additionalSegments.add(hkcazBuilder.build());

                if (dialogContext.doesOperationRequireTAN(SegmentType.HKCAZ)) {
                    additionalSegments.add(
                            HKTANv6.builder()
                                    .tanProcessVariant(
                                            HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                                    .segmentType(SegmentType.HKCAZ)
                                    .tanMediumName(dialogContext.getChosenTanMedium())
                                    .build());
                }

                return FinTsRequest.createEncryptedRequest(dialogContext, additionalSegments);
            };
}
