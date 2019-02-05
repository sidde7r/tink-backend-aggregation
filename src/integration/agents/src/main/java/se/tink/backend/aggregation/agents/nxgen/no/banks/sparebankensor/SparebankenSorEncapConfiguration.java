package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor;

import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConfiguration;

public class SparebankenSorEncapConfiguration implements EncapConfiguration {

    @Override
    public String getApplicationVersion() {
        return "11408";
    }

    @Override
    public String getEncapApiVersion() {
        return "3.3.5";
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return "SOR_MOBILBANK";
    }

    @Override
    public String getCredentialsBankCodeForEdb() {
        return "(null)";
    }

    @Override
    public String getSaIdentifier() {
        return "samobile_sor_ios_v1";
    }

    @Override
    public String getAppId() {
        return "no.sor.mobilbank-smbm";
    }

    @Override
    public String getRsaPubKeyString() {
        // found in PROD-esbi-server-pub.pem
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3maOiHUOhZR75rlXiyic"
                + "csi5mp5OEdkamnC1oRO1o71eP2u7v3i3sEIHQ9jHaIw6kHCrHqFCPvgjvbzcM8vC"
                + "uHZF3xafYCxShUH6Kb5AU7of6L7dTqXJDwyK6EJ1sGX1qIrlqVdYzDtfEES7NZb4"
                + "nJOpcFzeG9Nt9N7slm4Xq7KFYHFSkVXOWF2Se9f/raoaYVkFCNK8XClw1wPRnkc0"
                + "587xE1qwUa661m/pmCkm6M0FO7wfdS9zOQuq9Ual1x2sD7q+H2UhKtmY9zb31paM"
                + "ZDa6Tr3/eHopfisV/g1LxeVx/99tVf7b3vdAbBlcBep6YaawnhWM27NGEZ/jldzK"
                + "YQIDAQAB";
    }

    @Override
    // found in PROD-client-2811.pem
    public String getClientPrivateKeyString() {
        return "MIICXgIBAAKBgQDol+ZnqNrgOFh5tlRP8GCQD2HRaewnrPL57n8kbGZvDZkH93HS"
                + "pDFXgSbTVsgseLIMlPQtwKt7wzFPZDHvNgLucBsm/UD9iOEBttF4xJqpVLIp2juG"
                + "x37NRaeIZdD2eyK0PwEwTuiZyOkWurAGxGqrNezPZuQaha/x3aMuSqKEPQIDAQAB"
                + "AoGBAOP3JUrJ86Q5dXXtX1tuJKZtfRkL8Pq+BvMxbwna+Na1hByLyNKEPRwfqFcQ"
                + "wmbb8N5mC6DgKvLFUAoZENv8mwcLDyZeTaOowiWiJcpfinXA8OVY2NFHRElyl+2z"
                + "YE3LMwFFGizuXJ1M1HeBFU7Et5tQLf6VNwGXSSmg0lK0w6nhAkEA+HmjJ2i19a9b"
                + "pC8aWu8WsiGR7ibDH6U5rVPHeHSc/oOWP/fK3zzHkHdEXZ4Slnb5PpeoLmdczBNk"
                + "7xLpJ5MJ5QJBAO+jIqfCtdP3htlqt6J2z8t1FJjqTGKzC8QZKm4MWBoRs78itDcb"
                + "X1ZC0MZvbyC28bkO4MSvvoRZtMp0iG5+C3kCQBccuWBhDQvdU9jhyMHMm3/WP4y3"
                + "bk7zP3ov4M4DitbhxogMtIIvVSDK90D+Axyb1HNOCAbI9ojFBQ5349gahUECQQC1"
                + "dViWpBA/k93e4LpxIXqz29JfAHwa+O1d7ph8gIhuJR1xDHxehjGpBitFN7h0k1Nz"
                + "CFsXVOT1H2CRNbDhyjwxAkEAjxqO1+K1pqTncoznDhYZcZgwEQTnFrbACNOS8tvK"
                + "rVtut1XokPzjD77atT7yb9aSS23+Idr7vyu/SNPtNw+3Dw==";
    }
}
