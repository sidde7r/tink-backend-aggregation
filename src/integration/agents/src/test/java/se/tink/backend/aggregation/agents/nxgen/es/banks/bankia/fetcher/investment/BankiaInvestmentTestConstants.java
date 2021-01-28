package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.investment;

import org.junit.Ignore;

@Ignore
public class BankiaInvestmentTestConstants {

    public static String BALANCE_JSON =
            "{"
                    + "\"importeConSigno\": 0,"
                    + "\"numeroDecimalesImporte\": \"2\","
                    + "\"moneda\": {"
                    + "\"divisa\": \"281\","
                    + "\"digitoControlDivisa\": \"1\""
                    + "},"
                    + "\"nombreMoneda\": \"EUR\""
                    + "}";

    public static String ACCOUNT_TEMPLATE =
            "{\"contrato\": {\"identificadorContratoProductoInterno\": \""
                    + "%s"
                    + "\", \"identificadorContratoProducto\": "
                    + "\"%s\"}, \"saldoDisponible\": "
                    + BALANCE_JSON
                    + "}";

    public static String RESPONSE_TEMPLATE =
            "{"
                    + "\"datosRellamadaSalida\": \"%s\","
                    + "\"indicadorMasElementos\": %s,"
                    + "\"titulaciones\": [%s]"
                    + "}";

    public static String QUALIFICATION_JSON =
            "{"
                    + "\"descripcionEmision\": \"AA. AAA\","
                    + "\"identificadorBolsaMercado\": \"MCV\","
                    + "\"identificadorValor\": \"ES111111111\","
                    + "\"mercado\": \"CONTINUO\","
                    + "\"isin\": \"ES111111111\","
                    + "\"numeroDeposito\": \"0\","
                    + "\"participaciones\": 1,"
                    + "\"tipoActivo\": \"01\","
                    + "\"descripcionTipoActivo\": \"ACCIONES\","
                    + "\"valoracionTotal\": "
                    + BALANCE_JSON
                    + ","
                    + "\"valoracionUnitaria\": "
                    + BALANCE_JSON
                    + ","
                    + "\"valoracionTotalEUR\": "
                    + BALANCE_JSON
                    + ","
                    + "\"valoracionUnitariaEUR\": "
                    + BALANCE_JSON
                    + ""
                    + "}";
}
