package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccessInformationEntity {
    private boolean esPrimeraConexion;
    private boolean esCambioClave;
    private boolean esCambioFirma;
    private boolean tratarIndicadorFirmaContratoMultiacceso;
    private boolean indicadorRetornado;
    private String indicadorMultiaccesoInt;
    private boolean esNuevoCliente;
    private boolean esClienteSinFirma;
    private boolean esPrimerCambioDeClaveAcceso;
    private boolean esClienteDigital;
    private boolean indicadorMulticanalidad;
}
