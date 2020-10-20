package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.FetcherTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.IdentityUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.identity.BancoPostaIdentityFetcher;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

public class BancoPostaIdentityFetcherTest {
    public static final String IDENTITY_FETCHER_RESPONSE =
            "eyJraWQiOiI2Mzc4Nzc2MkUxQ0UxQzM1NjA4RkFFMzgxMzBCNjExODE3NTVBRTgyQTZBOTE2Q0EyMjRCRUE1MEMzRUZBQ0U2IiwiY3R5IjoiSldUIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJkdW1teU5hbWUuZHVtbXlMYXN0TmFtZS0xMjM0IiwiYXVkIjoiaHR0cHM6Ly9vaWRjLXByb3h5LnBvc3RlLml0IiwibmJmIjoxNjAyMDc3MTc4LCJhY3QiOnsic3ViIjoiZDhiN2ExNTYtMjE4Ny00OTE3LWI2M2UtMzI0NDNiN2ZjY2FiIiwiYWNyX3ZhbHVlcyI6Imh0dHBzOi8vaWRwLnBvc3RlLml0L0wwIiwiaXNzIjoiaHR0cHM6Ly9vaWRjLXByb3h5LnBvc3RlLml0In0sImFjcl92YWx1ZXMiOiJodHRwczovL2lkcC5wb3N0ZS5pdC9MMiIsImlzcyI6Imh0dHBzOi8vaWRwLXBvc3RlLnBvc3RlLml0IiwiY2xhaW1zIjp7ImJpcnRodG93bl9kZXNjcmlwdGlvbiI6IlBBTEVSTU8iLCJiaXJ0aGRhdGUiOiIyMDAwLTAxLTAxIiwibW9iaWxlTnVtYmVyIjoiZHVtbXlQaG9uZSIsInNleCI6Ik0iLCJwclJvbGVNZW1iZXIiOlsiaWQtcHJpdmF0YS1zdHJvbmcscG9ydGFsIiwibm90aWNlYm9hcmQscG9ydGFsIiwiaWQtcHJpdmF0YS1iYXNlLHBvcnRhbCIsInVzZXJzLHBvcnRhbCJdLCJ1c2VyaWQiOiJkdW1teU5hbWUuZHVtbXlMYXN0TmFtZS0xMjM0IiwiYmlydGhuYXRpb25fY29kZSI6IklUIiwiYmlydGhwcm92aW5jZV9jb2RlIjoiUEEiLCJzdXJuYW1lIjoiZHVtbXlMYXN0TmFtZSIsIm5hbWUiOiJkdW1teU5hbWUiLCJ0YXhjb2RlIjoiZHVtbXlTU04iLCJyZWFsbSI6InBvc3RlLml0IiwiYmlydGh0b3duX2NvZGUiOiIiLCJlbWFpbCI6ImZkdW1teUxhc3ROYW1lOTNAZ21haWwuY29tIiwiY2xpZW50X2NvZGUiOiJkdW1teUNsaWVudENvZGUifSwiZXhwIjoxNjAyMDc4MDc4LCJpYXQiOjE2MDIwNzcxNzgsIm5vbmNlIjoiM0Q3MDNBOUItM0Q4Mi00NjMzLUFFMzktQjczOTBERTI1QzE4IiwianRpIjoiNUEyN0UyMzctRDQ5OC00N0U3LTk4RTgtMTk3MUIzMjA5RUUzIiwic3RhdHVzIjoic3VjY2VzcyJ9.h3Eh9ifodcTxkIxOCfqs0tPrIRfmBNr8IxbojNoqiPnQ7VBp784YYNuzK2gmXvzlqjEThvRt2R3yebJWcf5gVAyzCyvzCwpezpvdDC54wQXICb5kPJsX658hubvzjJM5uyeZS92yE8fYAR7UxGDDJw2v3LZXcoaaLmSupOLWAlvRPlnwPr_1Wu9MNDI_TDR2qDr_ErPTOiTda5EQ3Q4ccWjkD4dPxf0LS2Iasiiz_O22nrzHtTcPTKEDaSaQCRjNuK_6eje98UPxRT8vRhe91FldemMbbQMPi_yPiMQ4YSUqm7X0rR_sIIvUDTbwL-9gFSpF2GN8db2mJyD2dsQFsw\n";

    @Test
    public void shouldFetchIdentityIfAvailable() {
        // given
        TinkHttpClient httpClient = mock(TinkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = FetcherTestHelper.prepareMockedPersistenStorage();
        BancoPostaStorage storage = new BancoPostaStorage(persistentStorage);
        BancoPostaApiClient apiClient = new BancoPostaApiClient(httpClient, storage);
        BancoPostaIdentityFetcher fetcher = new BancoPostaIdentityFetcher(apiClient);

        // when
        RequestBuilder fetchIdentityMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(IdentityUrl.FETCH_IDENTITY_DATA, httpClient);
        when(fetchIdentityMockRequestBuilder.post(any())).thenReturn(IDENTITY_FETCHER_RESPONSE);

        IdentityData identityData = fetcher.fetchIdentityData();

        // then
        assertThat(identityData.getFullName()).isEqualTo("dummyName dummyLastName");
        assertThat(identityData.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(identityData.getSsn()).isEqualTo("dummySSN");
    }
}
