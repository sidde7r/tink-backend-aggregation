package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RecurringPaymentInformationEntity {
    private String endDate;
    private String frequency;
    private String startDate;
}
