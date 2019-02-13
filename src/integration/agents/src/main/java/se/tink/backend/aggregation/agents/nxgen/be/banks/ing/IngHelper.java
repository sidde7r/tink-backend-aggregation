package se.tink.backend.aggregation.agents.nxgen.be.banks.ing;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.commons.lang.StringEscapeUtils;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entites.json.RequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.TypeReferences;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IngHelper {
    private final SessionStorage sessionStorage;
    private Map<String, String> urlsByRequestName;

    public IngHelper(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
        this.urlsByRequestName = Maps.newHashMap();
    }

    public void addRequestUrls(List<RequestEntity> requestsEntityList) {
        requestsEntityList.forEach(
                requestEntity ->
                        this.urlsByRequestName.put(
                                requestEntity.getName().toLowerCase(),
                                StringEscapeUtils.unescapeHtml(requestEntity.getUrl())));

        String urlsByRequestNameString =
                SerializationUtils.serializeToString(this.urlsByRequestName);
        this.sessionStorage.put(
                IngConstants.Storage.URLS_BY_REQUEST_NAME_STORAGE, urlsByRequestNameString);
    }

    public String getUrl(String requestName) {
        String requestUrl = this.urlsByRequestName.get(requestName);

        if (Strings.isNullOrEmpty(requestUrl)) {
            String urlsByRequestNameString =
                    this.sessionStorage.get(IngConstants.Storage.URLS_BY_REQUEST_NAME_STORAGE);
            this.urlsByRequestName =
                    SerializationUtils.deserializeFromString(
                            urlsByRequestNameString, TypeReferences.MAP_OF_STRING_STRING);
            requestUrl = this.urlsByRequestName.get(requestName);
        }

        return Preconditions.checkNotNull(
                requestUrl, "%s: %s", IngConstants.LogMessage.URL_NOT_FOUND, requestName);
    }

    public static Double parseAmountStringToDouble(String amount) {
        Preconditions.checkNotNull(amount);

        int scale = Character.getNumericValue(amount.charAt(amount.length() - 1));
        String amountWithoutScale = amount.substring(0, amount.length() - 1);

        BigDecimal bigDecimal = new BigDecimal(new BigInteger(amountWithoutScale), scale);
        return bigDecimal.doubleValue();
    }

    public static Predicate<AccountEntity> getAccountRulesPredicate(String rulesCode) {
        Preconditions.checkNotNull(rulesCode);
        return ae -> rulesCode.equalsIgnoreCase(ae.getRulesCode());
    }

    public void persist(LoginResponseEntity loginResponseEntity) {
        this.sessionStorage.put(IngConstants.Storage.LOGIN_RESPONSE, loginResponseEntity);
        addRequestUrls(loginResponseEntity.getRequests());
    }

    public Optional<LoginResponseEntity> retrieveLoginResponse() {
        return this.sessionStorage.get(
                IngConstants.Storage.LOGIN_RESPONSE, LoginResponseEntity.class);
    }

    public static boolean isLoginSuccessful(HttpResponse response) {
        return response.getBody(LoginResponseEntity.class) == null;
    }

    public static BaseResponse getLoginError(HttpResponse response) {
        return response.getBody(BaseResponse.class);
    }
}
