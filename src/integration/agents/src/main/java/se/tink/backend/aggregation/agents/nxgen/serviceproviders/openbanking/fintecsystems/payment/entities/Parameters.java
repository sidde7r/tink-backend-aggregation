package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@Getter
@Setter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Parameters {
    private String recipientHolder;
    private String recipientBic;
    private String recipientIban;
    private String recipientCountryId;
    private String recipientStreet;
    private String recipientZip;
    private String recipientCity;
    private String senderHolder;
    private String senderIban;
    private String senderBic;
    private String senderCountryId;
    private String amount;
    private String currencyId;
    private String purpose;
    private String recipientBankName;
    private String senderBankName;
    private String reconciliationKey;
}
