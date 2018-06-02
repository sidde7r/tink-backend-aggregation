package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchTransactionsResponseTestData {

    public static FetchTransactionsResponse getTestData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(responseBody, FetchTransactionsResponse.class);
    }

    private static String responseBody = "{"
            + "\"customBtd6ECOAS211F\": {"
            + "\"hayMas\": \"N\","
            + "\"fechasaldo\": 1485126000000,"
            + "\"saldo\": 48.29,"
            + "\"signosaldo\": \"H\","
            + "\"fechasaldoval\": 1508450400000,"
            + "\"saldoval\": 73.39,"
            + "\"signosaldoval\": \"H\","
            + "\"codmonedacta\": \"EUR\","
            + "\"indicadorRecibos\": \"0\","
            + "\"noccurspartemv\": 9,"
            + "\"customEccas211SPARTEMV\": [{"
            + "\"numSecuenMov\": 9,"
            + "\"fecmvtoEcrmvto2211\": 1485126000000,"
            + "\"conceptoMov\": \"Transferencia a Constantin Schwierz\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 2,"
            + "\"signoImporteMov\": \"D\","
            + "\"fecvalorEcrmvto221\": 1485126000000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 2,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 48.29,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 305,"
            + "\"contIdentOperServicio\": \"0832998451\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 8,"
            + "\"fecmvtoEcrmvto2211\": 1484089200000,"
            + "\"conceptoMov\": \"Transferencia de KARL ALFRED\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 3,"
            + "\"signoImporteMov\": \"H\","
            + "\"fecvalorEcrmvto221\": 1484089200000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 3,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 50.29,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 350,"
            + "\"contIdentOperServicio\": \"0831660613\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 7,"
            + "\"fecmvtoEcrmvto2211\": 1484002800000,"
            + "\"conceptoMov\": \"Transferencia a Benke\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 2,"
            + "\"signoImporteMov\": \"D\","
            + "\"fecvalorEcrmvto221\": 1484002800000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 2,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 47.29,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 305,"
            + "\"contIdentOperServicio\": \"0831390195\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 6,"
            + "\"fecmvtoEcrmvto2211\": 1482534000000,"
            + "\"conceptoMov\": \"Pago con 4B ...2607 6564, a las 00.00 h, en GUTE FAHRT MIT\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 0.79,"
            + "\"signoImporteMov\": \"D\","
            + "\"fecvalorEcrmvto221\": 1482534000000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 0.79,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 49.29,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 618,"
            + "\"contIdentOperServicio\": \"0040229201612240584350\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 5,"
            + "\"fecmvtoEcrmvto2211\": 1481842800000,"
            + "\"conceptoMov\": \"Pago con 4B ...2607 6564, a las 15.45 h, en SPAR BALBARD30\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 0.72,"
            + "\"signoImporteMov\": \"D\","
            + "\"fecvalorEcrmvto221\": 1481842800000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 0.72,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 50.08,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 618,"
            + "\"contIdentOperServicio\": \"0030300064466338681\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 4,"
            + "\"fecmvtoEcrmvto2211\": 1481842800000,"
            + "\"conceptoMov\": \"Transferencia a Benke\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 1.25,"
            + "\"signoImporteMov\": \"D\","
            + "\"fecvalorEcrmvto221\": 1481842800000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 1.25,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 50.8,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 305,"
            + "\"contIdentOperServicio\": \"0826356814\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 3,"
            + "\"fecmvtoEcrmvto2211\": 1481583600000,"
            + "\"conceptoMov\": \"Transferencia de KARL AL \","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 1.5,"
            + "\"signoImporteMov\": \"H\","
            + "\"fecvalorEcrmvto221\": 1481583600000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 1.5,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 52.05,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 350,"
            + "\"contIdentOperServicio\": \"0825636248\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 2,"
            + "\"fecmvtoEcrmvto2211\": 1481497200000,"
            + "\"conceptoMov\": \"Transferencia de KARL ALFRED \","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 1,"
            + "\"signoImporteMov\": \"H\","
            + "\"fecvalorEcrmvto221\": 1481497200000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 1,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 50.55,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 350,"
            + "\"contIdentOperServicio\": \"0825568062\","
            + "\"indicaRss\": \"\""
            + "}, {"
            + "\"numSecuenMov\": 1,"
            + "\"fecmvtoEcrmvto2211\": 1480633200000,"
            + "\"conceptoMov\": \"Orden de pago núm. 035967104, recibida de FLITIGA LISA\","
            + "\"tipoMov\": \"C\","
            + "\"importeMov\": 49.55,"
            + "\"signoImporteMov\": \"H\","
            + "\"fecvalorEcrmvto221\": 1480633200000,"
            + "\"codMoneda\": \"EUR\","
            + "\"importeCV\": 49.55,"
            + "\"codMonedaCV\": \"EUR\","
            + "\"indicador\": 0,"
            + "\"saldoCont\": 49.55,"
            + "\"signoCont\": \"H\","
            + "\"indE\": \"E\","
            + "\"refE\": 0,"
            + "\"indDuplicado\": \"\","
            + "\"codServicio\": 360,"
            + "\"contIdentOperServicio\": \"0007500000035967104001\","
            + "\"indicaRss\": \"\""
            + "}]"
            + "},"
            + "\"faultIndicator\": false,"
            + "\"faultMessage\": null,"
            + "\"faultCode\": null,"
            + "\"nextPage\": \"\""
            + "}";
}
