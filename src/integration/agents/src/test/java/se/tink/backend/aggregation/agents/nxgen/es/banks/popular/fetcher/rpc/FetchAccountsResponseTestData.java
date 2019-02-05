package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchAccountsResponseTestData {

    public static FetchAccountsResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(responseBody, FetchAccountsResponse.class);
    }

    private static String responseBody = "{"
            + "\"custom\": {"
            + "\"masOperaciones\": 0,"
            + "\"noccursdatostab\": 1,"
            + "\"customRr001014\": [{"
            + "\"numIntContrato\": 194181415,"
            + "\"iban\": \"ES95\","
            + "\"banco\": \"0075\","
            + "\"sucursal\": \"0128\","
            + "\"idExternaContrato\": \"32 06333285343\","
            + "\"fecSald\": 1508450400000,"
            + "\"posicion\": 73.39,"
            + "\"signoPosicion\": \"H\","
            + "\"monedaPosicion\": \"EUR\","
            + "\"fecSald2\": null,"
            + "\"posicion2\": 0,"
            + "\"signoPosicion2\": \"H\","
            + "\"monedaPosicion2\": \"\","
            + "\"fecSald3\": null,"
            + "\"posicion3\": 0,"
            + "\"signoPosicion3\": \"\","
            + "\"monedaPosicion3\": \"\","
            + "\"fecSald4\": null,"
            + "\"posicion4\": 0,"
            + "\"signoPosicion4\": \"\","
            + "\"monedaPosicion4\": \"\","
            + "\"tipoContrato\": \"CUENTA CORRIENTE\","
            + "\"marca\": 0,"
            + "\"modalidad\": 1,"
            + "\"producto\": 100,"
            + "\"situacion\": 0,"
            + "\"activacion\": 0,"
            + "\"numIntPrimerTitular\": 96952780,"
            + "\"nomTitContrato\": \"KARL ALFRED\","
            + "\"numIntSegundoTitular\": 0,"
            + "\"nomTitContrato2\": \"\","
            + "\"numIntTercerTitular\": 0,"
            + "\"nomTitContrato3\": \"\","
            + "\"numIntCuartoTitular\": 0,"
            + "\"nomTitContrato4\": \"\","
            + "\"indicatrans1\": 0,"
            + "\"indicapago\": 1,"
            + "\"indica1\": 0,"
            + "\"indica2\": 0,"
            + "\"indica3\": 0,"
            + "\"alias\": \"\","
            + "\"indDigDoc\": \"S\""
            + "}]"
            + "},"
            + "\"faultIndicator\": false,"
            + "\"faultMessage\": null,"
            + "\"faultCode\": null,"
            + "\"nextPage\": \"\""
            + "}";
}
