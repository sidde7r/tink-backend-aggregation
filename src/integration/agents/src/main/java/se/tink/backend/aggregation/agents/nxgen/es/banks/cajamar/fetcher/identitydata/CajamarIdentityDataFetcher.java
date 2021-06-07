package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.SessionKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.SplitValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
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
                apiClient
                        .getPositions()
                        .map(PositionEntity::getAccounts)
                        .map(fetchAccountIdentity())
                        .orElse("");

        EsIdentityDataBuilder builder = EsIdentityData.builder();
        if (!pdfData.equals("")) {
            builder = parseDocumentIdFromIdentityData(builder, pdfData);
        }

        return builder.setFullName(sessionStorage.get(SessionKeys.ACCOUNT_HOLDER_NAME))
                .setDateOfBirth(null)
                .build();
    }

    private Function<List<AccountEntity>, String> fetchAccountIdentity() {
        return accountEntities ->
                accountEntities.stream()
                        .findFirst()
                        .map(
                                accountEntity ->
                                        apiClient.fetchIdentityData(accountEntity.getAccountId()))
                        .map(CajamarIdentityDataResponse::getPdf)
                        .get();
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

        if (identityData.contains(SplitValues.PASSPORT)) {
            return builder.setPassportNumber(getDocumentId(identityData, SplitValues.PASSPORT));
        }

        return builder.setNifNumber(
                SplitValues.NIF.stream()
                        .filter(identityData::contains)
                        .map(s -> getDocumentId(identityData, s))
                        .findFirst()
                        .orElseGet(
                                () -> {
                                    log.warn(
                                            "Unmapped type of documentId with name of account holder and number of document");
                                    return "";
                                }));
    }

    private String getDocumentId(String identityData, String documentType) {
        int begin = identityData.indexOf(documentType);
        int end = findEndOfDocumentId(identityData, begin);
        String documentId = identityData.substring(begin, end).trim();
        return documentId.split(documentType)[1];
    }

    private int findEndOfDocumentId(String identityData, int begin) {
        int end = 0;
        for (String endOfDocumentId : SplitValues.END_OF_DOCUMENT_ID) {
            int endInDocument = identityData.indexOf(endOfDocumentId, begin);
            if (end == 0) {
                end = endInDocument;
                continue;
            }
            if (endInDocument != -1 && end > endInDocument) {
                end = endInDocument;
            }
        }
        return end;
    }
}
