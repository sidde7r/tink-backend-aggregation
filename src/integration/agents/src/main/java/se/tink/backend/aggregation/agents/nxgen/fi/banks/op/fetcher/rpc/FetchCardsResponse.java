package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

@JsonObject
public class FetchCardsResponse {
    private List<OpBankCardEntity> cardInfoList;

    @JsonIgnore
    public List<CreditCardAccount> getTinkCreditCards() {
        if (cardInfoList == null) {
            return Collections.emptyList();
        }

        return cardInfoList.stream()
                .filter(OpBankCardEntity::isCreditCard)
                .map(OpBankCardEntity::toTinkCardAccount)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<OpBankCardEntity> getCreditCardInfoList() {
        if (cardInfoList != null) {
            return cardInfoList.stream()
                    .filter(OpBankCardEntity::isCreditCard)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
    public List<OpBankCardEntity> getCardInfoList(){
        return this.cardInfoList;
    }
}
