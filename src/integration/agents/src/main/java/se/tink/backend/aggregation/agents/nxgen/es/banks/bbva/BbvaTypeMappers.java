package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public final class BbvaTypeMappers {
    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.CHECKING,
                            "0000015109", // CUENTA PERSONALES - CUENTA VA CONTIGO
                            "0000011954", // CUENTA NEGOCIOS - "personal" business account
                            "0CA0000079", // CUENTA BLUE
                            "0CA0000245", // CUENTA ON LINE
                            "0000009340", // CUENTA TRADER - marked as personal account by BBVA
                            "0CA0000299", // CUENTO UNO
                            "0000004272", // CUENTA BLUE ONLINE
                            "0GV0002307" // CUENTA BLUE ONLINE
                            )
                    .put(
                            AccountTypes.SAVINGS, "0000011102" // CUENTA METAS - goal account
                            )
                    .put(
                            AccountTypes.OTHER,
                            "0000009156", // RECARGA - PAGO MOVIL - prepaid card
                            "0000006412", // SEGURO PP. CONSUMO PUF - insurance
                            "0000009719" // TARJETA AHORA MASTERCARD - debit card
                            )
                    .put(
                            AccountTypes.PENSION,
                            "0000010644" // BBVA PLAN JUBILACION 2040, PPI - pension
                            )
                    .put(
                            AccountTypes.LOAN,
                            "0GV0002475", // PRESTAMO - RESTO CONSUMO CREDICONSUMO - consumer credit
                            "0GV0002307" // PRESTAMO - PRESTAMO 5,50% - loan
                            )
                    .put(
                            AccountTypes.CREDIT_CARD,
                            "0000010354", // CREDITO - REPSOL MAS CREDITO - credit card
                            "0000001315" // CREDITO - TARJETA DESPUES BLUE BBVA - credit card
                            )
                    .build();

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER_ALT =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.PENSION,
                            "00009", // PLANES DE PENSIONES - pension plan
                            "00027" // PLANES INDIVIDUALES - (individual) pension plan
                            )
                    .put(
                            AccountTypes.OTHER,
                            "00006", // TARJETAS PREPAGO - prepaid card
                            "00023", // RECARGA - prepaid card
                            "00003", // SEGUROS - insurance
                            "00020", // SEGUROS (RIESGO) - insurance
                            "00041", // SEGUROS DE PROTECCION PAGOS CONSUMO - insurance
                            "00005", // TARJETAS DE DEBITO - debit card
                            "00022" // DEBITO - debit card
                            )
                    .put(
                            AccountTypes.CHECKING, "00056" // CUENTAS PERSONALES - personal account
                            )
                    .put(
                            AccountTypes.LOAN, "00018" // PRESTAMO - loan
                            )
                    .put(
                            AccountTypes.CREDIT_CARD, "00016" // TARJETAS DE CREDITO - credit card
                            )
                    .build();
}
