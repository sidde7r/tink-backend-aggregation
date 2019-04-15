package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.identitydata.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AddressDataEntity {
    private String countryOfResidenceCode;
    private String countryOfResidenceName;
    private String nonCodedAddress1;
    private String nonCodedAddress3;
    private String nombreVia;
    private String numeroPortal;
    private String escaleraDomicilio;
    private String pisoDomicilio;
    private String puertaPiso;
    private String codigoProvincia;
    private String numeroDistritoPostal;
    private String codigoMunicipio;
    private String nombrePoblacion;
    private String nombreProvincia;
}
