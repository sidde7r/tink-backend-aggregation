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

    private static final String FIELD_NAME = "fieldsInJson";

    private final SupplementalInformationController supplementalInformationController;

    @Override
    public void authenticate(Credentials credentials) {
        String supplementalFieldsAsJson = credentials.getField(FIELD_NAME);
        List<List<Field>> supplementalScreens = parseInput(supplementalFieldsAsJson);
        askForInfo(supplementalScreens);
        // Everything finished, since this agent is just a shell, fail nicely
        throw LoginError.DEFAULT_MESSAGE.exception("CustomSupplemental demo finished.");
    }

    private List<List<Field>> parseInput(String fieldsAsJson) {
        List<List<Field>> allFields = tryParsingAsMultipleScreens(fieldsAsJson);
        if (allFields == null) {
            allFields = tryParsingAsSingleScreen(fieldsAsJson);
        }
        if (allFields == null) {
            allFields = tryParsingAsSingleField(fieldsAsJson);
        }

        if (allFields != null) {
            return allFields;
        }

        // We don't support anything else, give up
        throw LoginError.DEFAULT_MESSAGE.exception(
                "CustomSupplemental could not parse input as expected collection of fields");
    }

    private List<List<Field>> tryParsingAsMultipleScreens(String fieldsAsJson) {
        return SerializationUtils.deserializeFromString(
                fieldsAsJson, new TypeReference<List<List<Field>>>() {});
    }

    private List<List<Field>> tryParsingAsSingleScreen(String fieldsAsJson) {
        List<List<Field>> allFields = null;
        List<Field> fields =
                SerializationUtils.deserializeFromString(
                        fieldsAsJson, new TypeReference<List<Field>>() {});

        if (fields != null) {
            allFields = new ArrayList<>();
            allFields.add(fields);
        }
        return allFields;
    }

    private List<List<Field>> tryParsingAsSingleField(String fieldsAsJson) {
        List<List<Field>> allFields = null;
        Field field = SerializationUtils.deserializeFromString(fieldsAsJson, Field.class);

        if (field != null) {
            allFields = new ArrayList<>();
            List<Field> fields = new ArrayList<>();
            allFields.add(fields);
            fields.add(field);
        }
        return allFields;
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
