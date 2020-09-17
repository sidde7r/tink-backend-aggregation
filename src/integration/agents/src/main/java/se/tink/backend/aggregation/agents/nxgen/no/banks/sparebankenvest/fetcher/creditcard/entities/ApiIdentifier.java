package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.entities;

public class ApiIdentifier {
    private final String cardNumberGuid;
    private final String kidGuid;

    public ApiIdentifier(String cardNumberGuid, String kidGuid) {
        this.cardNumberGuid = cardNumberGuid;
        this.kidGuid = kidGuid;
    }

    public ApiIdentifier(String apiIdentifier) {
        if (apiIdentifier == null) {
            throw new IllegalStateException("Missing api identifier");
        }
        String[] apiIdentifierParts = apiIdentifier.split("\n");
        if (apiIdentifierParts.length != 2) {
            throw new IllegalStateException("Invalid api identifier " + apiIdentifier);
        }

        this.cardNumberGuid = apiIdentifierParts[0];
        this.kidGuid = apiIdentifierParts[1];
    }

    public String getCardNumberGuid() {
        return cardNumberGuid;
    }

    public String getKidGuid() {
        return kidGuid;
    }

    public String getApiIdentifier() {
        return String.format("%s%n%s", cardNumberGuid, kidGuid);
    }
}
