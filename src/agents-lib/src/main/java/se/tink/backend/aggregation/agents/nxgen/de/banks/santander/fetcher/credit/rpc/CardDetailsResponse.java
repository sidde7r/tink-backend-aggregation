package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities.MethodResult;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.core.Amount;

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
    return new Amount(methodResult.getSaldo().getdIVISA(), methodResult.getSaldo().getiMPORTE());
  }

  public CreditCardAccount toCreditCardAccount(String localContractDetail) {
    return CreditCardAccount.builder(
            methodResult.getMainCardPan(), getBalance(), getAvailableBalance())
        .setHolderName(new HolderName(methodResult.getCardHolderName()))
        .setAccountNumber(methodResult.getMainCardPan())
        .setName(methodResult.getProductName())
        .putInTemporaryStorage(
            SantanderConstants.STORAGE.LOCAL_CONTRACT_DETAIL, localContractDetail)
        .build();
  }
}
