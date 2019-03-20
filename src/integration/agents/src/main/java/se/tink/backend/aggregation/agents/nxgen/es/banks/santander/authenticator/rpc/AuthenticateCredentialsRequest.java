package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.rpc;

public class AuthenticateCredentialsRequest {

    public static String create(String username, String password) {
        return String.format(
                "<v:Envelope xmlns:v=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:c=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:d=\"http://www.w3.org/2001/XMLSchema\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">"
                        + "<v:Header />"
                        + "<v:Body>"
                        + "<n0:authenticateCredential xmlns:n0=\"http://www.isban.es/webservices/TECHNICAL_FACADES/Security/F_facseg_security/internet/loginServicesNSegSAN/v1\" facade=\"loginServicesNSegSAN\">"
                        + "<CB_AuthenticationData i:type=\":CB_AuthenticationData\">"
                        + "<documento i:type=\":documento\">"
                        + "<CODIGO_DOCUM_PERSONA_CORP i:type=\"d:string\">%s</CODIGO_DOCUM_PERSONA_CORP>"
                        + "<TIPO_DOCUM_PERSONA_CORP i:type=\"d:string\">%s</TIPO_DOCUM_PERSONA_CORP>"
                        + "</documento>"
                        + "<password i:type=\"d:string\">%s</password>"
                        + "</CB_AuthenticationData>"
                        + "<userAddress i:type=\"d:string\">127.0.0.1</userAddress>"
                        + "</n0:authenticateCredential>"
                        + "</v:Body>"
                        + "</v:Envelope>",
                username, getIdNumberType(username), password);
    }

    /**
     * Return "N" for NIF ID numbers and "C" for NIE ID numbers. NIF numbers start with a digit
     * while NIE numbers start with a letter.
     */
    private static String getIdNumberType(String username) {
        return startsWithDigit(username) ? "N" : "C";
    }

    private static boolean startsWithDigit(String username) {
        return username.substring(0, 1).matches("[\\d]");
    }
}
