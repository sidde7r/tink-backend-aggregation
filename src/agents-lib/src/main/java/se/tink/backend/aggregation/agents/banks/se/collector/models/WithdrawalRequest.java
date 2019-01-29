package se.tink.backend.aggregation.agents.banks.se.collector.models;

import se.tink.libraries.transfer.rpc.Transfer;

public class WithdrawalRequest {
    private double amount;
    private String description;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static WithdrawalRequest from(Transfer transfer) {
        WithdrawalRequest request = new WithdrawalRequest();
        request.setDescription(transfer.getDestinationMessage());
        request.setAmount(transfer.getAmount().getValue());

        return request;
    }
}
