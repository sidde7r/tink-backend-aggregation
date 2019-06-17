package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.TppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.enums.RedsysTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.executor.payment.entities.AddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.serializers.LocalDateDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class GetPaymentResponse {
    @JsonProperty private AmountEntity instructedAmount;
    @JsonProperty private AccountReferenceEntity debtorAccount;
    @JsonProperty private AccountReferenceEntity creditorAccount;
    @JsonProperty private String creditorName;
    @JsonProperty private String creditorAgent;
    @JsonProperty private AddressEntity creditorAddress;
    @JsonProperty private String chargeBearer;
    @JsonProperty private String remittanceInformationUnstructured;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate requestedExecutionDate;

    @JsonProperty private String transactionStatus;
    @JsonProperty private String psuMessage;
    @JsonProperty private List<TppMessageEntity> tppMessages;

    public RedsysTransactionStatus getTransactionStatus() {
        return RedsysTransactionStatus.fromString(transactionStatus);
    }

    @JsonIgnore
    public Amount getAmount() {
        return instructedAmount.toTinkAmount();
    }

    @JsonIgnore
    public String getCurrency() {
        return instructedAmount.toTinkAmount().getCurrency();
    }

    @JsonIgnore
    public AccountIdentifier getDebtorAccount() {
        return debtorAccount.toTinkAccountIdentifier();
    }

    @JsonIgnore
    public AccountIdentifier getCreditorAccount() {
        return creditorAccount.toTinkAccountIdentifier();
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public Optional<String> getRemittanceInformationUnstructured() {
        return Optional.ofNullable(Strings.emptyToNull(remittanceInformationUnstructured));
    }
}
