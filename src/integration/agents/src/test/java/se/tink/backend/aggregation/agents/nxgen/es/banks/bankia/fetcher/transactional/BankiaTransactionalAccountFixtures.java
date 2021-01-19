package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional;

import org.junit.Ignore;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public enum BankiaTransactionalAccountFixtures {
    DUMMY_ACCOUNTS(
            "{\n"
                    + "            \"contrato\": {\n"
                    + "                \"codigoProductoUrsus\": \"11594\",\n"
                    + "                \"codigoProductoPersonalizado\": \"11594\",\n"
                    + "                \"identificadorContratoProducto\": \"ES9121000418450200051332\",\n"
                    + "                \"entidadDelProducto\": \"2038\",\n"
                    + "                \"alias\": \"CUENTA ON\",\n"
                    + "                \"nivelOperatividad\": \"M\",\n"
                    + "                \"tipoRelacionContratoUsuario\": \"T\",\n"
                    + "                \"orden\": 0,\n"
                    + "                \"familia\": {\n"
                    + "                    \"idFamilia\": \"0003\",\n"
                    + "                    \"idSubfamilia\": \"-\"\n"
                    + "                },\n"
                    + "                \"idVista1\": true,\n"
                    + "                \"idVista2\": false,\n"
                    + "                \"idVista3\": false,\n"
                    + "                \"idVista4\": false,\n"
                    + "                \"indicadorProductoNuevo\": false,\n"
                    + "                \"numeroFirmasProducto\": 1,\n"
                    + "                \"indicadorTitularResidenteEnEspana\": true,\n"
                    + "                \"identificadorTipoNaturaleza\": \"I\",\n"
                    + "                \"oficinaContrato\": \"5866\",\n"
                    + "                \"decimalesParticipacionesFondo\": 0,\n"
                    + "                \"isin\": \"\"\n"
                    + "            },\n"
                    + "            \"saldoInformado\": true,\n"
                    + "            \"saldoReal\": {\n"
                    + "                \"importeConSigno\": 4301,\n"
                    + "                \"numeroDecimalesImporte\": \"2\",\n"
                    + "                \"moneda\": {\n"
                    + "                    \"divisa\": \"281\",\n"
                    + "                    \"digitoControlDivisa\": \"1\"\n"
                    + "                },\n"
                    + "                \"nombreMoneda\": \"EUR\"\n"
                    + "            },\n"
                    + "            \"saldoDisponible\": {\n"
                    + "                \"importeConSigno\": 4301,\n"
                    + "                \"numeroDecimalesImporte\": \"2\",\n"
                    + "                \"moneda\": {\n"
                    + "                    \"divisa\": \"281\",\n"
                    + "                    \"digitoControlDivisa\": \"1\"\n"
                    + "                },\n"
                    + "                \"nombreMoneda\": \"EUR\"\n"
                    + "            }\n"
                    + "        }"),
    DUMMY_ACCOUNT_DETAIL(
            "{\n"
                    + "    \"identificadorCuenta\": \"ES9121000418450200051332\",\n"
                    + "    \"intervinientes\": [\n"
                    + "        {\n"
                    + "            \"tipoDocumento\": \"1\",\n"
                    + "            \"identificadorDocumento\": \"0**HASHED:Bw**\",\n"
                    + "            \"nombreRazonSocial\": \"SZYMON MYSIAK\",\n"
                    + "            \"nombreApellidosCliente\": \"SZYMON MYSIAK\",\n"
                    + "            \"fechaNacimientoConstitucion\": {\n"
                    + "                \"valor\": \"1410-07-15\"\n"
                    + "            },\n"
                    + "            \"domicilio\": \"KOSZYKOWA 65\",\n"
                    + "            \"tipoRelacion\": \"020\",\n"
                    + "            \"descripcionTipoRelacion\": \"TITULAR\",\n"
                    + "            \"numeroRelacion\": \"1\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"rangoLiquidacion\": [],\n"
                    + "    \"fechaAperturaContrato\": {\n"
                    + "        \"valor\": \"2018-07-07\"\n"
                    + "    },\n"
                    + "    \"descripcionDisponibilidad\": \"DISPONIBLE\",\n"
                    + "    \"codigoMoneda\": \"281\",\n"
                    + "    \"nombreProducto\": \"CUENTA ON\",\n"
                    + "    \"domicilioCorrespondencia\": \"KOSZYKOWA 65\",\n"
                    + "    \"indicadorExisteFranquicia\": \"\",\n"
                    + "    \"liquidacionIntereses\": \"   3 MESES\"\n"
                    + "}");

    private final String json;

    BankiaTransactionalAccountFixtures(String json) {
        this.json = json;
    }

    public <T> T json(Class<T> aClass) {
        return SerializationUtils.deserializeFromString(this.json, aClass);
    }
}
