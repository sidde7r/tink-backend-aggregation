package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.creditcard.entities;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

public class CreditCard extends LinksResponse {

    private String number;
    private String name;
    private String cardId;
    private AmountEntity availableCredit;
    private String type;

    private Amount getAvailableCredit() {
        return availableCredit != null ? availableCredit.toTinkAmount() : Amount.inEUR(0d);
    }

    public Optional<LinkEntity> getDetailsLink() {
        return getLinks().findLink(SamlinkConstants.LinkRel.DETAILS);
    }

    public boolean hasCreated(CreditCardAccount account) {
        return cardId != null && cardId.equalsIgnoreCase(account.getAccountNumber());
    }

    public CreditCardAccount toTinkAccount(Amount balance) {
        return CreditCardAccount.builder(number, balance, getAvailableCredit())
                .setAccountNumber(cardId)
                .setName(name)
                .setBankIdentifier(transactionsLink())
                .build();
    }

    private String transactionsLink() {
        return getLinks().getLinkPath(SamlinkConstants.LinkRel.TRANSACTIONS);
    }
}
