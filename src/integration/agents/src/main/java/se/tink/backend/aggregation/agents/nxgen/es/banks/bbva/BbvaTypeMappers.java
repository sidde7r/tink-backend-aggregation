package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.account.enums.AccountFlag;

public final class BbvaTypeMappers {
    public static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CHECKING,
                            AccountFlag.PSD2_PAYMENT_ACCOUNT,
                            "0000015109", // CUENTA PERSONALES - CUENTA VA CONTIGO
                            "0000011954", // CUENTA NEGOCIOS - "personal" business account
                            "0CA0000079", // CUENTA BLUE
                            "0CA0000245", // CUENTA ON LINE
                            "0000009340", // CUENTA TRADER - marked as personal account by BBVA
                            "0CA0000299", // CUENTO UNO
                            "0000004272", // CUENTA BLUE ONLINE
                            "0CA0000089", // LIBRETON
                            "0CA0000028", // CUENTA ORDINARIA BBVA
                            "0000015652", // CUENTA DE PAGO ASOCIADA
                            "0CA0000014", // CUENTA EMPLEADOS BBVA
                            "0CA0000303", // CUENTA PERSONALES - CUENTA CORRIENTE UNO-E-ALTA
                            "0CA0000126", // CUENTA PERSONALES - CUENTA REMUNERADA UNO-E
                            "0CA0000304", // CUENTA PERSONALES - CUENTA CORRIENTE UNO-E-ALTA CON NIF
                            "0000016675", // CUENTA PERSONALES - CUENTA ELECCION
                            "0000015119", // CUENTA PERSONALES - MI SEGUNDA CUENTA
                            "0000006087", // CUENTA PERSONALES - CUENTA EMPLEADOS INTEGRACION
                            "0000004781", // CUENTA PERSONALES - CUENTA SELECCION COLECTIVOS
                            "0000008704", // CUENTA PERSONALES - CUENTA CORRIENTE UNO-E-ALTA DNI-NIE
                            // SIN OTE
                            "0000016231", //  CUENTA PERSONALES - CUENTA SIN COMISIONES PARA JOVENES
                            "0CA0000274", //  CUENTA PERSONALES - CUENTA 59+
                            "0CA0000277", // CUENTAS PERSONALES - CUENTA RENDIMIENTO BBVA
                            "0000015604", // CUENTAS PERSONALES - CUENTA NEGOCIOS BIENVENIDA
                            "0CA0000067", // CUENTAS PERSONALES - CUENTA EMPLEADOS EMPRESAS BBVA
                            "0CAX000006", // CUENTAS PERSONALES - CUENTAS DE AHORRO CON OPERATIVA
                            // RESTRINGIDA CX
                            "0CA0000113", // CUENTAS PERSONALES - CUENTA CORRIENTE UNO-E
                            "0000009701", // CUENTAS PERSONALES - CTA. RENDIMIENTO BBVA
                            "0CA0000305", // CUENTAS PERSONALES - CUENTA REINVERSION DIVIDENDOS
                            // BLUE"
                            "0000004273", // CUENTAS PERSONALES - CUENTA BLUE ON LINE
                            "0CAX000001", // CUENTAS PERSONALES - CUENTA INTEGRACION CX CONSUMIDOR
                            "0000016048", // CREDITO - CDC PYMES COVID-19
                            "0CA0000025", // CUENTAS PERSONALES - CUENTA REINVERSION DIVIDENDOS
                            "0CA0000116", // CUENTAS PERSONALES - CUENTA AHORRO UNO-E
                            "0CVD000003", // CUENTAS EN DIVISAS - CUENTAS VISTA EN DIVISA - is it
                            // correct mapping?
                            "0000010892", // CUENTAS PERSONALES - CUENTA FAMILIARES DE BBVA
                            "0CA0000103", // CUENTAS PERSONALES - COMPTE MED 1
                            "0CA0000294", // CUENTAS PERSONALES - CUENTA RENDIMIENTO BBVA
                            "0CA0000298", // CUENTAS PERSONALES - CUENTA ON LINE
                            "0CA0000078", // CUENTAS PERSONALES - CUENTA MINI BLUE
                            "0000015117", // CUENTAS PERSONALES - CUENTA ONLINE GO
                            "0000015790", // CUENTAS PERSONALES - BBVA CUENTA ON LINE BIENVENIDA
                            "0000011883", // CUENTAS PERSONALES - CUENTA VINCULADA
                            "0000010461", // CUENTAS PERSONALES - CUENTA RENDIMIENTO BBVA 2015
                            "0000010534" // CUENTAS PERSONALES - PRODUCTO CANCELADO CESE
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
                            "0000001315", // CREDITO - TARJETA DESPUES BLUE BBVA - credit card
                            "0000015059", // CREDITO - CUENTA DE CREDITO NEGOCIOS ONLINE
                            "0CC0000008" // CREDITO - CUENTA DE CREDITO TRES
                            )
                    .put(
                            AccountTypes.MORTGAGE,
                            "0CA0000105" // CUENTAS PERSONALES - CUENTA SOLUCION HIPOTECARIA
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
                            "00005" // TARJETAS DE DEBITO - debit card
                            // "00022" // DEBITO - debit card
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

    public static final TypeMapper<LoanDetails.Type> LOAN_TYPE_MAPPER =
            TypeMapper.<LoanDetails.Type>builder()
                    .put(
                            LoanDetails.Type.BLANCO,
                            "00021", // PRESTAMO 5,50% & RESTO CONSUMO CREDICONSUMO
                            "00022" // PRESTAMO ONLINE NEGOCIOS
                            )
                    .build();
}
