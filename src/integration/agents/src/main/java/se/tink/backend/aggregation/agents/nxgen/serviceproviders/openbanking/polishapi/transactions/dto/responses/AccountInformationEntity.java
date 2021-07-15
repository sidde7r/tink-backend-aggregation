package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountInformationEntity {
    private String accountNumber;
    private String accountMassPayment;

    // depending on the implementation status might be an object or String
    private String name;
    private NameAddressEntity nameAddress;
}
