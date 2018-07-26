package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.entities.HandelsbankenSEAccountContext;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transferdestination.entities.PaymentRecipient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class HandelsbankenSEPaymentContext extends BaseResponse {

    private HandelsbankenSEAccountContext fromAccounts;
    private List<PaymentRecipient> recipients;

    public List<GeneralAccountEntity> retrieveOwnedSourceAccounts() {
        return Optional.ofNullable(fromAccounts)
                .flatMap(HandelsbankenSEAccountContext::asOwnedAccountEntities)
                .orElse(Collections.emptyList());
    }

    public List<GeneralAccountEntity> retrieveDestinationAccounts() {
        return Optional.ofNullable(recipients)
                .map(Collection::stream)
                .map(recipients -> recipients
                        .map(PaymentRecipient::retrieveGeneralAccountEntities)
                        .collect(Collectors.toList())
                )
                .orElse(Collections.emptyList());
    }

    public class Failure extends BaseResponse {

        public boolean customerIsUnder16() {
            return HandelsbankenSEConstants.Fetcher.Transfers.UNDER_16.equals(getCode());
        }
    }
}
