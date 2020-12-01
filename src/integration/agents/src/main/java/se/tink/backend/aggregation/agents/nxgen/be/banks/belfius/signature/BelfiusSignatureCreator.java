package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.signature;

import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.persistence.BelfiusAuthenticationData;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;

public class BelfiusSignatureCreator {

    public String createSignatureSoft(String challenge, String deviceToken, String panNumber) {
        String challengeUpperCase = challenge.toUpperCase();
        String panNumberStripped = panNumber.replace(" ", "");

        return hash(
                String.format(
                        "%s%s%s%s",
                        challengeUpperCase, challengeUpperCase, deviceToken, panNumberStripped));
    }

    public String createSignaturePw(
            String challenge, String contractNumber, BelfiusAuthenticationData authData) {
        return createSignaturePw(
                challenge,
                authData.getDeviceToken(),
                authData.getPanNumber(),
                contractNumber,
                authData.getPassword());
    }

    public String createSignaturePw(
            String challenge,
            String deviceToken,
            String panNumber,
            String contractNumber,
            String password) {
        String challengeUpperCase = challenge.toUpperCase();
        String panNumberStripped = panNumber.replace(" ", "");

        String contractPasswordHash = hash(String.format("%s%s", contractNumber, password));

        String hashInput =
                String.format(
                        "%s%s%s%s%s",
                        challengeUpperCase,
                        challengeUpperCase,
                        deviceToken,
                        panNumberStripped,
                        contractPasswordHash);

        return hash(hashInput);
    }

    // challenge|iban(85)|iban(to/94)|amount(0,01) EUR
    public String createTransferSignature(
            String challenge,
            String sourceIban,
            String destinationIban,
            String amount,
            String currency) {
        String toSha = challenge + sourceIban + destinationIban + amount + " " + currency;
        return hash(toSha);
    }

    public String hash(String data) {
        return Hash.sha1AsHex(data).toUpperCase();
    }
}
