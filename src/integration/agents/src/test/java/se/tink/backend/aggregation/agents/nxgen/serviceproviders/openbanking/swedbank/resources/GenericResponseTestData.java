package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.resources;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class GenericResponseTestData {

    public static final GenericResponse INVALID_KYC =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"KYC_INVALID\",\n"
                            + "      \"text\": \"Please direct customer to the online banking to fill KYC.\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse INTERNET_BANK_AGREEMENT =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"INTERNET_BANK_AGREEMENT\",\n"
                            + "      \"text\": \"Missing bank agreement error message\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse MISSING_BANK_ID =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"MISSING_BANK_ID\",\n"
                            + "      \"text\": \"Bank Id error message\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse BANKID_ALREADY_IN_PROGRESS =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"FORMAT_ERROR\",\n"
                            + "      \"text\": \"Other login session is ongoing\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse NO_PROFILE =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"FORMAT_ERROR\",\n"
                            + "      \"text\": \"No profile available\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse WRONG_USER_ID =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"FORMAT_ERROR\",\n"
                            + "      \"text\": \"Wrong UserId parameter\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse AUTHORIZATION_EXPIRED =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"FORMAT_ERROR\",\n"
                            + "      \"text\": \"Authorization expired\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);

    public static final GenericResponse TOKEN_EXPIRED =
            SerializationUtils.deserializeFromString(
                    "{\n"
                            + "  \"tppMessages\": [\n"
                            + "    {\n"
                            + "      \"category\": \"ERROR\",\n"
                            + "      \"code\": \"TOKEN_EXPIRED\",\n"
                            + "      \"text\": \"Provided refresh_token expired\"\n"
                            + "    }\n"
                            + "  ]\n"
                            + "}",
                    GenericResponse.class);
}
