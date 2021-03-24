package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Optional;
import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.entity.Balance;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.executor.beneficiary.entity.CreditorType;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryRequest;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.rpc.Beneficiary;

@Builder
@JsonPropertyOrder({
    "balance",
    "creditorType",
    "designationLabel",
    "holderIndicator",
    "iban",
    "reference"
})
public class CaisseEpargneCreateBeneficiaryRequest {
    @JsonProperty("reference")
    private String reference;

    @JsonProperty("creditorType")
    private CreditorType creditorType;

    @JsonProperty("balance")
    private Balance balance;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("holderIndicator")
    private boolean isOwnAccount;

    @JsonProperty("designationLabel")
    private String name;

    @JsonIgnore
    public static <T extends CreateBeneficiaryRequest>
            Optional<CaisseEpargneCreateBeneficiaryRequest> of(T createBeneficiaryRequest) {
        Beneficiary beneficiary = createBeneficiaryRequest.getBeneficiary().getBeneficiary();
        if (!AccountIdentifierType.IBAN.equals(beneficiary.getAccountNumberType())) {
            return Optional.empty();
        }
        return Optional.of(
                CaisseEpargneCreateBeneficiaryRequest.builder()
                        .iban(beneficiary.getAccountNumber())
                        .reference(beneficiary.getAccountNumber())
                        .name(beneficiary.getName())
                        .isOwnAccount(false)
                        .creditorType(CreditorType.builder().code("2").label("IBAN").build())
                        .balance(Balance.builder().currencyCode("EUR").build())
                        .build());
    }
}
