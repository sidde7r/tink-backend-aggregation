package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import no.finn.unleash.UnleashContext;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.libraries.mapper.PrioritizedValueExtractor;
import se.tink.libraries.unleash.model.Toggle;
import se.tink.libraries.unleash.strategies.aggregation.providersidsandexcludeappids.Constants;

public class CreditCardBalanceMapperFactory {

    public static CreditCardBalanceMapper get(AgentComponentProvider componentProvider) {
        Toggle toggle =
                Toggle.of("natwest-group-credit-card-balance")
                        .context(
                                UnleashContext.builder()
                                        .addProperty(
                                                Constants.Context.PROVIDER_NAME.getValue(),
                                                componentProvider.getContext().getProviderId())
                                        .build())
                        .build();

        if (componentProvider.getUnleashClient().isToggleEnable(toggle)) {

            return new DefaultCreditCardBalanceMapper(new PrioritizedValueExtractor());
        } else {
            return new NatwestGroupCreditCardBalanceMapper();
        }
    }
}
