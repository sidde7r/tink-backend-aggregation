package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities.AccountInfoEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public final class RedsysDetailedConsentGeneratorBuilder {

    public interface ComponentProviderStep {
        AvailableScopeStep componentProvider(AgentComponentProvider componentProvider);
    }

    public interface AvailableScopeStep {
        SpecifyAccountStep availableScopes(Set<RedsysScope> availableScopes);
    }

    public interface SpecifyAccountStep {
        FinalStep forSpecifiedAccount(String iban);

        FinalStep forUserSpecifiedAccounts();
    }

    public interface FinalStep {
        RedsysDetailedConsentGenerator build();
    }

    static class Steps
            implements ComponentProviderStep, AvailableScopeStep, SpecifyAccountStep, FinalStep {

        private AgentComponentProvider componentProvider;
        private Set<RedsysScope> availableScopes;
        private List<AccountInfoEntity> accountInfoEntities;

        public AvailableScopeStep componentProvider(AgentComponentProvider componentProvider) {
            this.componentProvider = componentProvider;
            return this;
        }

        @Override
        public SpecifyAccountStep availableScopes(Set<RedsysScope> availableScopes) {
            this.availableScopes = availableScopes;
            return this;
        }

        @Override
        public FinalStep forSpecifiedAccount(String iban) {
            this.accountInfoEntities = Collections.singletonList(new AccountInfoEntity(iban));
            return this;
        }

        @Override
        public FinalStep forUserSpecifiedAccounts() {
            this.accountInfoEntities = Collections.emptyList();
            return this;
        }

        @Override
        public RedsysDetailedConsentGenerator build() {
            return new RedsysDetailedConsentGenerator(
                    componentProvider, availableScopes, accountInfoEntities);
        }
    }
}
