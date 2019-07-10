package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.enums.BunqPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class PaymentResponseEntity {

    private String id;
    private String status;
    private List<EntryEntity> entries;

    public List<PaymentResponse> toTinkPaymentResponses() {

        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        return entries.stream().map(e -> toTinkPaymentResponse(e)).collect(Collectors.toList());
    }

    public PaymentResponse toTinkPaymentResponse() {

        if (entries == null || entries.isEmpty()) {
            return null;
        }

        return toTinkPaymentResponse(entries.get(0));
    }

    private PaymentResponse toTinkPaymentResponse(EntryEntity entry) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(entry.toTinkCreditor())
                        .withDebtor(entry.toTinkDebtor())
                        .withAmount(
                                Amount.valueOf(
                                        entry.getAmount().getCurrency(),
                                        Double.valueOf(
                                                        Double.parseDouble(
                                                                        entry.getAmount()
                                                                                .getValue())
                                                                * 100)
                                                .longValue(),
                                        2))
                        .withExecutionDate(null)
                        .withCurrency(entry.getAmount().getCurrency())
                        .withUniqueId(id)
                        .withStatus(
                                BunqPaymentStatus.mapToTinkPaymentStatus(
                                        BunqPaymentStatus.fromString(status)));

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
