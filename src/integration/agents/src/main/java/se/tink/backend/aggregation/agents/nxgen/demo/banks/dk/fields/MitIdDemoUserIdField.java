package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk.fields;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MitIdDemoUserIdField {

    public static Field build() {
        return Field.builder()
                .name("mitIdDemoAskUserIdField")
                .description("Please enter your MitID user ID")
                .helpText(
                        "Your MitID user ID is the name you are identified by when you log on or approve using MitID.")
                .minLength(5)
                .maxLength(48)
                .pattern("^[abcdefghijklmnopqrstuvwxyzæøå0123456789{}!#$^,*()_+-=:;?.@ ]{5,48}$")
                .patternError(
                        "User ID must be made up of the following symbols: abcdefghijklmnopqrstuvwxyzæøå0123456789{}!#$^,*()_+-=:;?.@ and spacebar.")
                .build();
    }
}
