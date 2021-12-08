package se.tink.agent.sdk.user_interaction.swedish_mobile_bankid;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import javax.annotation.Nullable;

@JsonSerialize(using = SwedishMobileBankIdInfoSerializer.class)
public class SwedishMobileBankIdInfo {
    @Nullable final String autostartToken;

    private SwedishMobileBankIdInfo(@Nullable String autostartToken) {
        this.autostartToken = autostartToken;
    }

    public static SwedishMobileBankIdInfo withAutostartToken(String autostartToken) {
        return new SwedishMobileBankIdInfo(autostartToken);
    }

    public static SwedishMobileBankIdInfo withoutAutostartToken() {
        return new SwedishMobileBankIdInfo(null);
    }
}
