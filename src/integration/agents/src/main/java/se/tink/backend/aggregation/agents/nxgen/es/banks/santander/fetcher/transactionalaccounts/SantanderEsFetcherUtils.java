package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

public class SantanderEsFetcherUtils {

    public static String getIsPaginationString(boolean isPagination) {
        return isPagination ? "S" : "N";
    }

    public static String formatRequestForTransactionFetching(
            String userDataXmlString, String contractIdXmlString, String balancaXmlString) {

        return String.format(
                "<datosConexion>%s</datosConexion>"
                        + "<contratoID>%s</contratoID>"
                        + "<importeCta>%s</importeCta>",
                userDataXmlString, contractIdXmlString, balancaXmlString);
    }
}
