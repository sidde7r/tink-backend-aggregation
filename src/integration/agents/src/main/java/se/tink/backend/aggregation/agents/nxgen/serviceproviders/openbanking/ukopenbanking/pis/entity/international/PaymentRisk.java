package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.international;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRisk {
    private String paymentContextCode;
    private DeliveryAddress deliveryAddress;
    private String merchantCategoryCode;
    private String merchantCustomerIdentification;
}
