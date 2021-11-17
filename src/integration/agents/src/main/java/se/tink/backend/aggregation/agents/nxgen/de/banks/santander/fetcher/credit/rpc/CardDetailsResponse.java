package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.MethodResult;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.utils.CreditCardMasker;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.cryptography.hash.Hash;

@JsonObject
public class CardDetailsResponse {

    @JsonProperty("methodResult")
    private MethodResult methodResult;

    private ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(
                methodResult.getAvailableAmount().getiMPORTE(),
                methodResult.getAvailableAmount().getdIVISA());
    }

    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(
                methodResult.getSaldo().getiMPORTE(), methodResult.getSaldo().getdIVISA());
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
