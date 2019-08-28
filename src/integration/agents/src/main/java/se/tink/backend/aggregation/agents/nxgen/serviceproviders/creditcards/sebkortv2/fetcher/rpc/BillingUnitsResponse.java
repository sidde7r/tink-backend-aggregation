package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity.BillingUnitEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class BillingUnitsResponse {
    private List<BillingUnitEntity> body;

    public List<CreditCardAccount> getCreditCardAccounts(
            SebKortConfiguration config, String currency) {
        if (Objects.isNull(body)) {
            return Collections.emptyList();
        }

        return body.stream()
                .map(
                        billingUnitEntity ->
                                billingUnitEntity.createCreditCardAccount(config, currency))
                .collect(Collectors.toList());
    }
}
