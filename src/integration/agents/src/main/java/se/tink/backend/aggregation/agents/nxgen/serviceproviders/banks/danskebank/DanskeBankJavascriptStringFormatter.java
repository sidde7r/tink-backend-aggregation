package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

public final class DanskeBankJavascriptStringFormatter {
    private static final String COMMON_FUNCTION =
            "var nt = new Function(\"eval(\" + JSON.stringify(e) + \");\\n      return { ValidateSignature_v2:\\n          typeof ValidateSignature_v2 === 'function'\\n          ? ValidateSignature_v2\\n          : (function() {}),\\n        AcceptSignature_v2:\\n          typeof AcceptSignature_v2 === 'function'\\n          ? AcceptSignature_v2\\n          : (function() {}),\\n        initStepUp:\\n          typeof initStepUp === 'function'\\n          ? initStepUp\\n          : (function() {}),\\n        initStepUpTrustedDevice:\\n          typeof initStepUpTrustedDevice === 'function'\\n          ? initStepUpTrustedDevice\\n          : (function() {}),\\n        valiStepUp:\\n          typeof valiStepUp === 'function'\\n          ? valiStepUp\\n          : (function() {}),\\n        validateStepUpTrustedDevice:\\n          typeof validateStepUpTrustedDevice === 'function'\\n          ? validateStepUpTrustedDevice\\n          : (function() {}),\\n        resetServiceCode:\\n          typeof resetServiceCode === 'function'\\n          ? resetServiceCode\\n          : (function() {}),\\n        setServiceCodeRules:\\n          typeof setServiceCodeRules === 'function'\\n          ? setServiceCodeRules\\n          : (function() {}),\\n        setServiceCodeRulesUec:\\n          typeof setServiceCodeRulesUec === 'function'\\n          ? setServiceCodeRulesUec\\n          : (function() {}),\\n        verifyChangeServiceCode:\\n          typeof verifyChangeServiceCode === 'function'\\n          ? verifyChangeServiceCode\\n          : (function() {}),\\n        changeServiceCodeUec:\\n          typeof changeServiceCodeUec === 'function'\\n          ? changeServiceCodeUec\\n          : (function() {}),\\n        getChangeServiceCodeRules:\\n          typeof getChangeServiceCodeRules === 'function'\\n          ? getChangeServiceCodeRules\\n          : (function() {}),\\n        generateResponse:\\n          typeof generateResponse === 'function'\\n          ? generateResponse\\n          : (function() {}),\\n        decryptDeviceSecret:\\n          typeof decryptDeviceSecret === 'function'\\n          ? decryptDeviceSecret\\n          : (function() {}),\\n        decryptActivationCode:\\n          typeof decryptActivationCode === 'function'\\n          ? decryptActivationCode\\n          : (function() {}),\\n        encryptCredentials:\\n          typeof encryptCredentials === 'function'\\n          ? encryptCredentials\\n          : (function() {}),\\n        initSignSEBankID:\\n          typeof initSignSEBankID === 'function'\\n          ? initSignSEBankID\\n          : (function() {}),\\n        pollSeBankId:\\n          typeof pollSeBankId === 'function'\\n          ? pollSeBankId\\n          : (function() {}),\\n        init: (typeof init === 'function' ? init : function () {}),\\n        cleanup: (typeof cleanup === 'function' ? cleanup : function () {}),\\n        setAppReadyState:\\n          typeof setAppReadyState === 'function'\\n          ? setAppReadyState\\n          : (function() {})\\n      };\")();";

    public static String createBankIdJavascript(String dynamicBankIdJavascript, String ssn) {
        String javascriptString =
                "var e = %s;\n"
                        + "var nt = new Function(\"eval(\" + JSON.stringify(e) + \"); return {\\n                  init: (typeof init === 'function' ? init : function () {}),\\n                  initSeBankIdLogon: initSeBankIdLogon,\\n                  pollSeBankId: pollSeBankId,\\n                  cleanup: (typeof cleanup === 'function' ? cleanup : function () {})\\n                 };\")();\n"
                        + "function getLogonPackage(logonPackage, failMethod) {\n"
                        + "    document.body.setAttribute(\"logonPackage\", logonPackage);"
                        + "}\n"
                        + "function getFinalizePackage(finalizePackage, failMethod) {\n"
                        + "    document.body.setAttribute(\"finalizePackage\", finalizePackage)"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.initSeBankIdLogon(\"%s\", getLogonPackage, failMethod);\n"
                        + "nt.pollSeBankId(\"OK\", getFinalizePackage, failMethod)";

        return String.format(javascriptString, dynamicBankIdJavascript, ssn);
    }

    public static String createLoginJavascript(
            String dynamicLogonJavascript, String username, String password) {
        String javascriptString =
                "var e = %s;\n"
                        + "var nt = new Function(\"eval(\" + JSON.stringify(e) + \"); return {\\n                  performLogonServiceCode_v2: performLogonServiceCode_v2,\\n                  init: (typeof init === 'function' ? init : function () {}),\\n                  cleanup: (typeof cleanup === 'function' ? cleanup : function () {})\\n                 };\")();\n"
                        + "function getFinalizePackage(finalizePackage, failMethod) {\n"
                        + "    document.body.setAttribute(\"finalizePackage\", JSON.stringify(finalizePackage))"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.performLogonServiceCode_v2(\"%s\", \"%s\", getFinalizePackage, failMethod);";

        return String.format(javascriptString, dynamicLogonJavascript, username, password);
    }

    public static String createChallengeJavascript(
            String challengeDynamicJavascript, String username, String otpChallenge) {
        String challengeJavascript =
                "var e = %s;\n"
                        + COMMON_FUNCTION
                        + "function setChallengeInfo(challengeInfoEntity) {\n"
                        + "    document.body.setAttribute(\"challengeInfo\", JSON.stringify(challengeInfoEntity));\n"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.initStepUp(\"%s\", \"%s\", setChallengeInfo, failMethod);";

        return String.format(
                challengeJavascript, challengeDynamicJavascript, username, otpChallenge);
    }

    public static String createChallengeAnswerJavascript(
            String challengeDynamicJavascript, String challengeAnswer) {
        String challengeAnswerJavascript =
                "var e = %s;\n"
                        + COMMON_FUNCTION
                        + "function setStepUpToken(stepUpToken) {\n"
                        + "    document.body.setAttribute(\"bindStepUpToken\", JSON.stringify(stepUpToken));\n"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.valiStepUp(\"%s\", setStepUpToken, failMethod);";

        return String.format(
                challengeAnswerJavascript, challengeDynamicJavascript, challengeAnswer);
    }

    public static String createCollectDeviceSecretJavascript(
            String challengeDynamicJavascript, String sharedSecret) {
        String collectDeviceSecretJavascript =
                "var e = %s;\n"
                        + COMMON_FUNCTION
                        + "function setDecryptedDeviceSecret(decryptedDeviceSecret) {\n"
                        + "    document.body.setAttribute(\"decryptedDeviceSecret\", JSON.stringify(decryptedDeviceSecret));\n"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.decryptDeviceSecret(\"%s\", !0, setDecryptedDeviceSecret, failMethod);";

        return String.format(
                collectDeviceSecretJavascript, challengeDynamicJavascript, sharedSecret);
    }

    public static String createInitStepUpTrustedDeviceJavascript(
            String challengeDynamicJavascript, String username, String otpChallenge) {
        String initStepUpTrustedDeviceJavascript =
                "var e = %s;\n"
                        + COMMON_FUNCTION
                        + "function setChallengeInfo(challengeInfoEntity) {\n"
                        + "    document.body.setAttribute(\"trustedChallengeInfo\", JSON.stringify(challengeInfoEntity));\n"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.initStepUpTrustedDevice(\"%s\", \"%s\", setChallengeInfo, failMethod);";

        return String.format(
                initStepUpTrustedDeviceJavascript,
                challengeDynamicJavascript,
                username,
                otpChallenge);
    }

    public static String createGenerateResponseJavascript(
            String challengeDynamicJavascript,
            String username,
            String otpChallenge,
            String generateResponseInputAsBase64) {
        String generateResponseJavascript =
                "var e = %s;\n"
                        + COMMON_FUNCTION
                        + "function setChallengeInfo(challengeInfoEntity) {\n"
                        + "    document.body.setAttribute(\"trustedChallengeInfo\", JSON.stringify(challengeInfoEntity));\n"
                        + "}\n"
                        + "function setChallengeResponse(challengeResponse) {\n"
                        + "    document.body.setAttribute(\"trustedChallengeResponse\", JSON.stringify(challengeResponse));\n"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.initStepUpTrustedDevice(\"%s\", \"%s\", setChallengeInfo, failMethod);"
                        + "nt.generateResponse(\"%s\", setChallengeResponse, failMethod);";

        return String.format(
                generateResponseJavascript,
                challengeDynamicJavascript,
                username,
                otpChallenge,
                generateResponseInputAsBase64);
    }

    public static String createValidateStepUpTrustedDeviceJavascript(
            String challengeDynamicJavascript,
            String username,
            String otpChallenge,
            String generateResponseInputAsBase64,
            String validateStepUpTrustedDeviceAsBase64) {
        String validateStepUpTrustedDeviceJavascript =
                "var e = %s;\n"
                        + COMMON_FUNCTION
                        + "function setChallengeInfo(challengeInfoEntity) {\n"
                        + "    document.body.setAttribute(\"trustedChallengeInfo\", JSON.stringify(challengeInfoEntity));\n"
                        + "}\n"
                        + "function setChallengeResponse(challengeResponse) {\n"
                        + "    document.body.setAttribute(\"trustedChallengeResponse\", JSON.stringify(challengeResponse));\n"
                        + "}\n"
                        + "function setStepUpToken(stepUpToken) {\n"
                        + "    document.body.setAttribute(\"trustedStepUpToken\", JSON.stringify(stepUpToken));\n"
                        + "}\n"
                        + "function failMethod(arg1) {}\n"
                        + "nt.initStepUpTrustedDevice(\"%s\", \"%s\", setChallengeInfo, failMethod);"
                        + "nt.generateResponse(\"%s\", setChallengeResponse, failMethod);"
                        + "nt.validateStepUpTrustedDevice(\"%s\", setStepUpToken, failMethod);";

        return String.format(
                validateStepUpTrustedDeviceJavascript,
                challengeDynamicJavascript,
                username,
                otpChallenge,
                generateResponseInputAsBase64,
                validateStepUpTrustedDeviceAsBase64);
    }
}
