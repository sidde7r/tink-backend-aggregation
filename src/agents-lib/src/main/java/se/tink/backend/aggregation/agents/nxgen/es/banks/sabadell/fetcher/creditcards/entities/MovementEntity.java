package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MovementEntity {
    private CardMovementEntity cardMovement;

    public CardMovementEntity getCardMovement() {
        return cardMovement;
    }
}
