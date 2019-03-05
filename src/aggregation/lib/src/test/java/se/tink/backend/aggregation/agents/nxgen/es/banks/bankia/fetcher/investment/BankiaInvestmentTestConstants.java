package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment;

public class BankiaInvestmentTestConstants {

    public static String BALANCE_JSON = "{\n" +
            "\t\t\t\"importeConSigno\": 0,\n" +
            "\t\t\t\"numeroDecimalesImporte\": \"2\",\n" +
            "\t\t\t\"moneda\": {\n" +
            "\t\t\t\t\"divisa\": \"281\",\n" +
            "\t\t\t\t\"digitoControlDivisa\": \"1\"\n" +
            "\t\t\t},\n" +
            "\t\t\t\"nombreMoneda\": \"EUR\"\n" +
            "\t\t}";

    public static String ACCOUNT_TEMPLATE = "{\"contrato\": {\"identificadorContratoProductoInterno\": \""
            + "%s"
            + "\", \"identificadorContratoProducto\": " +
            "\"%s\"}, \"saldoDisponible\": "
            + BALANCE_JSON
            + "}";

    public static String RESPONSE_TEMPLATE = "{\n" +
            "\t\"datosRellamadaSalida\": \"%s\",\n" +
            "\t\"indicadorMasElementos\": %s,\n" +
            "\t\"titulaciones\": [%s]\n" +
            "}";


    public static String QUALIFICATION_JSON = "{\n" +
            "\t\t\"descripcionEmision\": \"AA. AAA\",\n" +
            "\t\t\"identificadorBolsaMercado\": \"MCV\",\n" +
            "\t\t\"identificadorValor\": \"ES111111111\",\n" +
            "\t\t\"mercado\": \"CONTINUO\",\n" +
            "\t\t\"isin\": \"ES111111111\",\n" +
            "\t\t\"numeroDeposito\": \"0\",\n" +
            "\t\t\"participaciones\": 1,\n" +
            "\t\t\"tipoActivo\": \"01\",\n" +
            "\t\t\"descripcionTipoActivo\": \"ACCIONES\",\n" +
            "\t\t\"valoracionTotal\": " + BALANCE_JSON + ",\n" +
            "\t\t\"valoracionUnitaria\": " + BALANCE_JSON + ",\n" +
            "\t\t\"valoracionTotalEUR\": " + BALANCE_JSON + ",\n" +
            "\t\t\"valoracionUnitariaEUR\": " + BALANCE_JSON + "\n" +
            "\t}";

}
