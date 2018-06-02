package se.tink.libraries.abnamro.client.rpc;

public class CreateSubscriptionRequest {

    private String bcNumber;

    public CreateSubscriptionRequest(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    public String getBcNumber() {
        return bcNumber;
    }
}
