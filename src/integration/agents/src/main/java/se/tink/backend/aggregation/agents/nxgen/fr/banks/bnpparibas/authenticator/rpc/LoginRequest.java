package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

@Builder
public class LoginRequest extends AbstractForm {

    private String username;
    private String gridId;
    private String passwordIndices;
    private String userAgent;
    private String gridType;
    private String idFaValue;
    private String idFvValue;
    private String distId;
    private String appVersion;
    private String buildNumber;

    private LoginRequest(
            String username,
            String gridId,
            String passwordIndices,
            String userAgent,
            String gridType,
            String idFaValue,
            String idFvValue,
            String distId,
            String appVersion,
            String buildNumber) {
        this.username = username;
        this.gridId = gridId;
        this.passwordIndices = passwordIndices;
        this.userAgent = userAgent;
        this.gridType = gridType;
        this.idFaValue = idFaValue;
        this.idFvValue = idFvValue;
        this.distId = distId;
        this.appVersion = appVersion;
        this.buildNumber = buildNumber;

        this.put(BnpParibasConstants.Auth.AUTH, buildAuthFormValue());
    }

    private String buildAuthFormValue() {

        String challengeResponseString = buildChallengeResponseString();
        String userAgentString = buildUserAgentString();
        String deviceInfoString = buildDeviceInfoString();
        String appString = buildAppString();

        return challengeResponseString + userAgentString + deviceInfoString + appString;
    }

    private String buildChallengeResponseString() {
        String b64EncodedUsername = EncodingUtils.encodeAsBase64String(username);

        return String.format(
                "<DIST_ID>%s</DIST_ID>"
                        + "<MEAN_ID>%s</MEAN_ID>"
                        + "<EAI_AUTH_TYPE>%s</EAI_AUTH_TYPE>"
                        + "<EBANKING_USER_ID>"
                        + "<PERS_ID>%s</PERS_ID>"
                        + "<SMID>%s</SMID>"
                        + "</EBANKING_USER_ID>"
                        + "<CHALLENGE_RESPONSE>"
                        + "<VALUE>%s</VALUE>"
                        + "<CHALLENGE>%s</CHALLENGE>"
                        + "<AUTH_FACTOR_ID>%s</AUTH_FACTOR_ID>"
                        + "</CHALLENGE_RESPONSE>"
                        + "<CHALLENGE_RESPONSE>"
                        + "<VALUE>%s</VALUE>"
                        + "<CHALLENGE>%s</CHALLENGE>"
                        + "<AUTH_FACTOR_ID>%s</AUTH_FACTOR_ID>"
                        + "</CHALLENGE_RESPONSE>"
                        + "<CHALLENGE_RESPONSE>"
                        + "<VALUE>%s</VALUE>"
                        + "<CHALLENGE>%s</CHALLENGE>"
                        + "<AUTH_FACTOR_ID>%s</AUTH_FACTOR_ID>"
                        + "</CHALLENGE_RESPONSE>"
                        + "<CHALLENGE_RESPONSE>"
                        + "<VALUE>%s</VALUE>"
                        + "<CHALLENGE>%s</CHALLENGE>"
                        + "<AUTH_FACTOR_ID>%s</AUTH_FACTOR_ID>"
                        + "</CHALLENGE_RESPONSE>"
                        + "<CHALLENGE_RESPONSE>"
                        + "<VALUE>[%s]</VALUE>"
                        + "<CHALLENGE>%s</CHALLENGE>"
                        + "<AUTH_FACTOR_ID>[%s]</AUTH_FACTOR_ID>"
                        + "</CHALLENGE_RESPONSE>",
                distId,
                BnpParibasConstants.AuthFormValues.MEAN_ID,
                BnpParibasConstants.AuthFormValues.MEAN_ID,
                b64EncodedUsername,
                b64EncodedUsername,
                gridId,
                BnpParibasConstants.AuthFormValues.ID_GRILLE,
                gridId,
                gridType,
                BnpParibasConstants.Auth.GRID_TYPE,
                gridType,
                passwordIndices,
                BnpParibasConstants.AuthFormValues.POS_SELECT,
                passwordIndices,
                BnpParibasConstants.AuthFormValues.VALUE_1,
                BnpParibasConstants.AuthFormValues.IDB64,
                BnpParibasConstants.AuthFormValues.VALUE_1,
                BnpParibasConstants.AuthFormValues.HCE,
                BnpParibasConstants.AuthFormValues.HCE,
                BnpParibasConstants.AuthFormValues.HCE);
    }

    private String buildUserAgentString() {
        return String.format(
                "<USER_AGENT>"
                        + "<VALUE>%s</VALUE>"
                        + "<IPADDRESS>%s</IPADDRESS>"
                        + "</USER_AGENT>",
                userAgent, BnpParibasConstants.AuthFormValues.IP_ADDRESS);
    }

    private String buildDeviceInfoString() {
        return String.format(
                "<DEVICE_INFO>"
                        + "<SCREEN>"
                        + "<AVAIL_HEIGHT>667</AVAIL_HEIGHT>"
                        + "<AVAIL_WIDTH>375</AVAIL_WIDTH>"
                        + "<HEIGHT>667</HEIGHT>"
                        + "<WIDTH>375</WIDTH>"
                        + "<PIXEL_DEPTH>2.0</PIXEL_DEPTH>"
                        + "<DENSITY>[DEVICE_INFO__SCREEN__DENSITY]</DENSITY>"
                        + "</SCREEN>"
                        + "<DEVICE>"
                        + "<TERMINAL_OS_NAME>%s</TERMINAL_OS_NAME>"
                        + "<TERMINAL_OS_VERSION>%s</TERMINAL_OS_VERSION>"
                        + "<TERMINAL_NAME>%s</TERMINAL_NAME>"
                        + "<TERMINAL_BRAND>%s</TERMINAL_BRAND>"
                        + "<TERMINAL_LANGUAGE>%s</TERMINAL_LANGUAGE>"
                        + "<IMSI>[DEVICE_INFO__DEVICE__IMSI]</IMSI>"
                        + "<ANDROID>"
                        + "<ROOT_MODE>[DEVICE_INFO__DEVICE__ANDROID__ROOT_MODE]</ROOT_MODE>"
                        + "<AAID>[DEVICE_INFO__DEVICE__ANDROID__AAID]</AAID>"
                        + "<UDID>[DEVICE_INFO__DEVICE__ANDROID__UDID]</UDID>"
                        + "<IMEI>[DEVICE_INFO__DEVICE__ANDROID__IMEI]</IMEI>"
                        + "<INSTANCE_ID>[DEVICE_INFO__DEVICE__ANDROID__INSTANCE_ID]</INSTANCE_ID>"
                        + "</ANDROID>"
                        + "<IOS>"
                        + "<COLOR_DEPTH>[DEVICE_INFO__DEVICE__IOS__COLOR_DEPTH]</COLOR_DEPTH>"
                        + "<DEVICE_ID>[DEVICE_INFO__DEVICE__IOS__DEVICE_ID]</DEVICE_ID>"
                        + "<IDFA>%s</IDFA>"
                        + "<TERMINAL_CONNECTION>[DEVICE_INFO__DEVICE__IOS__TERMINAL_CONNECTION]</TERMINAL_CONNECTION>"
                        + "<IDFV>%s</IDFV>"
                        + "</IOS>"
                        + "</DEVICE>"
                        + "</DEVICE_INFO>",
                BnpParibasConstants.AuthFormValues.OS_NAME,
                BnpParibasConstants.AuthFormValues.IOS_VERSION,
                BnpParibasConstants.AuthFormValues.DEVICE,
                BnpParibasConstants.AuthFormValues.BRAND,
                BnpParibasConstants.AuthFormValues.LANGUAGE,
                idFaValue,
                idFvValue);
    }

    private String buildAppString() {
        return String.format(
                "<APP>"
                        + "<VERSION>%s</VERSION>"
                        + "<BUILD_NUMBER>%s</BUILD_NUMBER>"
                        + "<PLATEFORM>%s</PLATEFORM>"
                        + "</APP>",
                appVersion, buildNumber, BnpParibasConstants.AuthFormValues.PLATFORM);
    }
}
