package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.utils;

public class SoapHelper {
    public static String createSsoBapiRequest(String acessToken, String termId) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                + "<soap:Body>"
                + "<sso_BAPI xmlns=\"http://caisse-epargne.fr/webservices/\">"
                + "<modeALD>0</modeALD>"
                + "<idTerminal>"
                + termId
                + "</idTerminal>"
                + "<etablissement>CAISSE_EPARGNE</etablissement>"
                + "<at>"
                + acessToken
                + "</at>"
                + "</sso_BAPI>"
                + "</soap:Body>"
                + "</soap:Envelope>";
    }
}
