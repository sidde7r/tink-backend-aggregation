package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata;

import java.io.IOException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.SessionKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.SplitValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata.rpc.CajamarIdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

@Slf4j
public class CajamarIdentityDataFetcher implements IdentityDataFetcher {

    private final CajamarApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CajamarIdentityDataFetcher(CajamarApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        String pdfData =
                apiClient.fetchPositions().getAccounts().stream()
                        .findFirst()
                        .map(
                                accountEntity ->
                                        apiClient.fetchIdentityData(accountEntity.getAccountId()))
                        .map(CajamarIdentityDataResponse::getPdf)
                        .get();
        EsIdentityDataBuilder builder = EsIdentityData.builder();
        builder = parseDocumentIdFromIdentityData(builder, pdfData);

        return builder.setFullName(sessionStorage.get(SessionKeys.ACCOUNT_HOLDER_NAME))
                .setDateOfBirth(null)
                .build();
    }

    private EsIdentityDataBuilder parseDocumentIdFromIdentityData(
            EsIdentityDataBuilder builder, String pdfData) {
        String identityData;
        try {
            byte[] decoder = Base64.getDecoder().decode(pdfData);
            PDDocument doc = PDDocument.load(decoder);
            PDFTextStripper pdf = new PDFTextStripper();
            identityData = pdf.getText(doc);

        } catch (IOException e) {
            throw new IllegalStateException("Could not deserialize or decrypt extra", e);
        }

        if (identityData.indexOf(SplitValues.NIF) != 0) {
            return builder.setNifNumber(getDocumentId(identityData, SplitValues.NIF));
        }
        if (identityData.indexOf(SplitValues.PASSPORT) != 0) {
            return builder.setPassportNumber(getDocumentId(identityData, SplitValues.PASSPORT));
        }
        log.info(
                "Unmapped type of documentId with name of account holder and number of document: "
                        + getUnmappedDocumentId(identityData));
        return builder.setDocumentNumber("");
    }

    private String getDocumentId(String identityData, String documentType) {
        int begin = identityData.indexOf(documentType);
        int end = identityData.indexOf(SplitValues.END_OF_DOCUMENT_ID, begin);
        String documentId = identityData.substring(begin, end).trim();
        return documentId.split(documentType)[1];
    }

    private String getUnmappedDocumentId(String identityData) {
        int begin = identityData.indexOf(SplitValues.ADDITIONAL_PARSER);
        int end = identityData.indexOf(SplitValues.ADDITIONAL_END_PARSER, begin);
        return identityData.substring(begin, end);
    }
}
