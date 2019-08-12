package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BunqConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String redirectUrl;

    @JsonProperty @Secret private String psd2ApiKey;

    @JsonProperty @Secret private String clientId;

    @JsonProperty @SensitiveSecret private String clientSecret;

    @JsonProperty @Secret private String psd2InstallationKeyPair;

    @JsonProperty @Secret private String psd2ClientAuthToken;

    public static String getDescriptionPsd2InstallationKeyPair() {
        return "This is the serialized java.security.KeyPair that is used in the first\n"
                + "step under the 'Register as a service provider' header in Bunq's documentation\n"
                + "(https://beta.doc.bunq.com/psd2/connect-as-a-psd2-service-provider).\n"
                + "We use the following method to do the serialization:\n"
                + "    public static String serializeKeyPair(KeyPair kp) {\n"
                + "        PublicKey pubKey = kp.getPublic();\n"
                + "        PrivateKey privKey = kp.getPrivate();\n"
                + "\n"
                + "        Map<String, String> m = new HashMap<String, String>();\n"
                + "        m.put(\"alg\", privKey.getAlgorithm());\n"
                + "        m.put(\"pubKey\", Hex.encodeHexString(pubKey.getEncoded()));\n"
                + "        m.put(\"privKey\", Hex.encodeHexString(privKey.getEncoded()));\n"
                + "        return serializeToString(m);\n"
                + "    }";
    }

    public static String getExamplePsd2InstallationKeyPair() {
        return "'{\"privKey\":\"308204bc020100300d06092a864886f70d0101010500048204a6308204a2020100028201010091c0765c9f92114b9914ca9ad80e958e4c52d75582b3525a3e7c5e74612d95e2f64dbdb4ef45cc49fb8fc3a38354ec48919ba2483c80108c1080c6e9310b366bf86b3fbc96a38ce03ddfddf5f9baf0871cfd1cf9a832984fd2a4b02f3620b71e133bb83553161d68c7b4575be4063f11cd56edfca4a9d82bf1d6cd98c934854f4f50bcd9a5874dafe1e51d23ea178696f728b5fe406dee10b1e61bae187a6be6492b6f18b0c0498248a1ad6018fdb76cc0bb5a2b8173d63cb134b72ee6e6b12193f93576b0729939e1788e1cf3ba72294c552694e0e069d2f0fa19f392ff173793c6bf85628df881426338b48cd2efed093dc9d1f4ebf4ae09eb3cbf05020c5b0203010001028201005b3589dc64af4fcb733ca39d4adfb58f9b0bac5d82b11d34042e681d8442b6ad955d08fe954da48e17b2dbc71384ad68ac562cb3efc8649282315448d1bc268e62a31afbb5791bcba86cd3c5fe459207bb8859fd91d837a09c3d1b5430174100bd207e2240a0723f03da4360e9cd4ee2a1116954d4e6c4b49b5ec4c27c0735f47da7f422d7c78311ee0626a81736883c864916126e0e7fa773c4b6c846df054fbdfecb1c6f2fa4698ae1c2781902cafd011499433ee0e8a0e8807ed243e49deac6579d13454ce8609dc4f8cb375c3c53e5a88f281917f860015a41527415d335eda3a47a775ee52f1bd4230574d9be20f487941b622e2913866d0cbaf2dd66e102818100cb2d974aa3d416d7398236939499416ef1d8ee2df80c4f7156497ede7352425a4616eb87b99e701ac8b159394c5f8082d88f661b02df102778f0951257b59690e4a2d482b75584e5b4146bdb38e1dd2a28e69f1672c83d8bf3d3efc5d4c5d1ff9dedb7e544c7942622397c55e3921c99cb5671b67e92c52b9c01332ef100ee9502818100b7a4e5529bdd57cba4773edcd9a253eb21dd85f9a7879c5694305be98d4aa472d92ae0ed75657da9d1ea00d8807f21048c34141323e533221247d5312428e0be0cfa862345f0a017841238c6e8ea7c7a184b04ee362a3da7c7ba4f5a20bf346a9d9ffd2cf03fcf3531456206462f1ab7b8fe7c5e236b446c22c18b45ecc0832f0281806c2db4160bb08b10c10ff19b164d9b274521a97f5448c9aba0b3b5171ff3b65c766111bacc3bed98752ee840b7c749976152c89cad24b36730df779fb28f7ec15adacfa76191e9169c911af4380f1a40ae524c76195b4059d9d308211f2ecca6fb216cfe6766941cd1b025e15d2f9886d25b0e648a73ff033f26ec679bee5119028180710b4a36bdc754c2006d7efec7e14ab314d79e1efde7566f5d2d1f86f6f8999e30dc5a3511ecf59bd1a1afff95a9ac137692a040dff9b8ae227ea1576ca0d5276829d6e463ab298110457f39c22a85914adef31c07af9ce068ebe0e8e3fb8f3289f0f011f799efdd1885533ff5780d84469630cc6a151fbb48f1c6dfb64b12f5028180491ee9acd7941e1fa8d1f4ae4af71c4344d6d2ae9804031c1b34168c8f9d4e82de291baddecc65bfe945107492579cb8cf363187886ef6395f5a59f1ab6b5970de5aaf8f6f50d71a40637343dc8c338406cb33d0f57e3eaad4b793fe8517f04b1de5a035fad56fe10fbdfcda261e307a8dce80a982b5d4a7bd553795388d432e\",\"alg\":\"RSA\",\"pubKey\":\"30820122300d06092a864886f70d01010105000382010f003082010a028201010091c0765c9f92114b9914ca9ad80e958e4c52d75582b3525a3e7c5e74612d95e2f64dbdb4ef45cc49fb8fc3a38354ec48919ba2483c80108c1080c6e9310b366bf86b3fbc96a38ce03ddfddf5f9baf0871cfd1cf9a832984fd2a4b02f3620b71e133bb83553161d68c7b4575be4063f11cd56edfca4a9d82bf1d6cd98c934854f4f50bcd9a5874dafe1e51d23ea178696f728b5fe406dee10b1e61bae187a6be6492b6f18b0c0498248a1ad6018fdb76cc0bb5a2b8173d63cb134b72ee6e6b12193f93576b0729939e1788e1cf3ba72294c552694e0e069d2f0fa19f392ff173793c6bf85628df881426338b48cd2efed093dc9d1f4ebf4ae09eb3cbf05020c5b0203010001\"}'";
    }

    public static String getDescriptionPsd2ClientAuthToken() {
        return "This is the serialized token that you get as a response from the call you make in the first\n"
                + "step under the 'Register as a service provider' header in Bunq's documentation\n"
                + "(https://beta.doc.bunq.com/psd2/connect-as-a-psd2-service-provider). We use the following\n"
                + "function to do the serialization:\n"
                + "    public static <T> String serializeToString(T value) {\n"
                + "        try {\n"
                + "            return new ObjectMapper()\n"
                + "                 .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)\n"
                + "                 .writeValueAsString(value);\n"
                + "        } catch (JsonProcessingException e) {\n"
                + "            log.error(\"Could not serialize object\", e);\n"
                + "            return null;\n"
                + "        }\n"
                + "    }";
    }

    public static String getExamplePsd2ClientAuthToken() {
        return "'{\"id\":1893862,\"created\":\"2019-05-29 12:39:40.707959\",\"updated\":\"2019-05-29 12:39:40.707959\",\"token\":\"5551ebd29c973a4271fdd414f7a25725185aefbc98b29209f4654a42cd55b88b\"}'";
    }

    public static String getDescriptionPsd2ApiKey() {
        return "This is the API key you received after step 2 as it is explained under the\n"
                + "'Register as a service provider' header in Bunq's documentation\n"
                + "(https://beta.doc.bunq.com/psd2/connect-as-a-psd2-service-provider).";
    }

    public static String getExamplePsd2ApiKey() {
        return "f6615c1c118be917fb5500bac41234567890cec092985a29b912345678904c66";
    }

    public String getPsd2InstallationKeyPair() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2InstallationKeyPair),
                String.format(
                        BunqConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "PSD2 Installation Key Pair"));

        return psd2InstallationKeyPair;
    }

    public String getPsd2ClientAuthToken() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2ClientAuthToken),
                String.format(
                        BunqConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "PSD2 Client Auth Token"));

        return psd2ClientAuthToken;
    }

    public String getPsd2ApiKey() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psd2ApiKey),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "PSD2 Api Key"));

        return psd2ApiKey;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(BunqConstants.ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
