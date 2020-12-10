package se.tink.backend.aggregation.agents.nxgen.demo.banks.customsupplemental.authenticator;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class CustomSupplementalAuthenticator implements Authenticator {

    private final SupplementalInformationController supplementalInformationController;

    @Override
    public void authenticate(Credentials credentials) {
        String supplementalFieldsAsJson = credentials.getField(Field.Key.USERNAME);
        List<List<Field>> supplementalScreens = parseInput(supplementalFieldsAsJson);
        askForInfo(supplementalScreens);
        // Everything finished, since this agent is just a shell, fail nicely
        throw LoginError.DEFAULT_MESSAGE.exception("CustomSupplemental demo finished.");
    }

    private List<List<Field>> parseInput(String supplementalFieldsAsJson) {
        // Try parsing as multiple screens
        List<List<Field>> allFields =
                SerializationUtils.deserializeFromString(
                        supplementalFieldsAsJson, new TypeReference<List<List<Field>>>() {});

        // If that failed, try parsing as single screen
        if (allFields == null) {
            List<Field> fields =
                    SerializationUtils.deserializeFromString(
                            supplementalFieldsAsJson, new TypeReference<List<Field>>() {});
            if (fields != null) {
                allFields = new ArrayList<>();
                allFields.add(fields);
            }
        }

        // Or as a single Field
        if (allFields == null) {
            Field field =
                    SerializationUtils.deserializeFromString(supplementalFieldsAsJson, Field.class);
            if (field != null) {
                allFields = new ArrayList<>();
                List<Field> fields = new ArrayList<>();
                allFields.add(fields);
                fields.add(field);
            }
        }

        // We don't support anything else, give up
        if (allFields == null) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "CustomSupplemental could not parse input as expected collection of fields");
        } else {
            return allFields;
        }
    }

    private void askForInfo(List<List<Field>> supplementalScreens) {
        for (List<Field> screen : supplementalScreens) {
            if (screen.size() != 0) {
                supplementalInformationController.askSupplementalInformation(
                        screen.toArray(new Field[0]));
            }
        }
    }
}
