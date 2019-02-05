package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdEntity {
    private int regNo;
    private long accountNo;

    public static String createUniqueIdentifier(IdEntity id) {
        return Optional.ofNullable(id).map(IdEntity::asUniqueIdentifier).orElse("00");
    }

    private String asUniqueIdentifier() {
        return String.valueOf(regNo + accountNo);
    }
}
