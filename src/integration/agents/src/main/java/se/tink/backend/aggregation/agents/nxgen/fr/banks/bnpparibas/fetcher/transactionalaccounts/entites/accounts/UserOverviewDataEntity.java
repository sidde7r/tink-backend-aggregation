package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserOverviewDataEntity {
    @JsonProperty("abonnement")
    private SubscriptionEntity subscription;

    private CustomerEntity client;

    @JsonProperty("contrat")
    private ContractEntity contract;

    private EvaluationClientEntity evaluationClient;

    @JsonProperty("indicateurs")
    private IndicatorsEntity indicators;

    @JsonProperty("informationsIdentification")
    private LoginInformationEntity loginInformation;

    private EnrolmentEntity enrolement;

    public SubscriptionEntity getSubscription() {
        return subscription;
    }

    public CustomerEntity getClient() {
        return client;
    }

    public ContractEntity getContract() {
        return contract;
    }

    public EvaluationClientEntity getEvaluationClient() {
        return evaluationClient;
    }

    public IndicatorsEntity getIndicators() {
        return indicators;
    }

    public LoginInformationEntity getIdentificationInformation() {
        return loginInformation;
    }

    public EnrolmentEntity getEnrolement() {
        return enrolement;
    }
}
