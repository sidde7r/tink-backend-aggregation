package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInfo;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.AccountInformationListItem;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.GetAccountInformationListResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.core.Amount;

public final class GetAccountInformationListResponse {
    private Envelope envelope;

    public GetAccountInformationListResponse(Envelope envelope) {
        this.envelope = envelope;
    }

    public Map<String, Amount> getAccountNumberToBalanceMap() {
        return Optional.ofNullable(envelope)
                .map(Envelope::getBody)
                .map(Body::getGetAccountInformationListResponseEntity)
                .map(GetAccountInformationListResponseEntity::getOk)
                .map(OK::getAccountInformationListItemList)
                .map(Stream::of).orElse(Stream.empty())
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(AccountInformationListItem::getAccountInfo)
                .filter(Objects::nonNull)
                .filter(a -> a.getAccountNumber() != null)
                .filter(a -> a.getAccountCurrency() != null)
                .filter(a -> a.getAmountEntity() != null)
                .filter(a -> a.getAmountEntity().getAmount() != null)
                .collect(Collectors.toMap(
                        AccountInfo::getAccountNumber,
                        accountInfo -> new Amount(
                                accountInfo.getAccountCurrency(),
                                accountInfo.getAmountEntity().getAmount()
                        )
                ));
    }
}
