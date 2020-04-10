package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount.FinTsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKCAZv1;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKKAZv5;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;

public interface TransactionRequestBuilder {

    FinTsRequest build(
            FinTsDialogContext dialogContext,
            FinTsAccountInformation account,
            String startingPoint);

    static TransactionRequestBuilder getRequestBuilder(
            FinTsTransactionFetcher.StrategyType strategy, int version) {
        if (strategy == FinTsTransactionFetcher.StrategyType.CAMT && version == 1) {
            return CAMT_1;
        } else if (strategy == FinTsTransactionFetcher.StrategyType.SWIFT && version == 5) {
            return SWIFT_5;
        }
        throw new IllegalArgumentException(
                String.format(
                        "Couldn't find request builder for strategy: %s, version: %d",
                        strategy, version));
    }

    TransactionRequestBuilder SWIFT_5 =
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

    TransactionRequestBuilder CAMT_1 =
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
