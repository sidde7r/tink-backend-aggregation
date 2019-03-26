package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.MethodResult;
import se.tink.backend.aggregation.agents.utils.crypto.Hash;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.utils.CreditCardMasker;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CardDetailsResponse {

    @JsonProperty("methodResult")
    private MethodResult methodResult;

    private Amount getAvailableBalance() {
        return new Amount(
                methodResult.getAvailableAmount().getdIVISA(),
                methodResult.getAvailableAmount().getiMPORTE());
    }

    private Amount getBalance() {
        return new Amount(
                methodResult.getSaldo().getdIVISA(), methodResult.getSaldo().getiMPORTE());
    }

    public CreditCardAccount toCreditCardAccount(String localContractDetail) {
        return CreditCardAccount.builder(
                        Hash.sha1AsHex(methodResult.getMainCardPan()),
                        getBalance(),
                        getAvailableBalance())
                .setHolderName(new HolderName(methodResult.getCardHolderName()))
                .setAccountNumber(CreditCardMasker.maskCardNumber(methodResult.getMainCardPan()))
                .setName(methodResult.getProductName())
                .putInTemporaryStorage(
                        SantanderConstants.STORAGE.LOCAL_CONTRACT_DETAIL, localContractDetail)
                .build();
    }
}
