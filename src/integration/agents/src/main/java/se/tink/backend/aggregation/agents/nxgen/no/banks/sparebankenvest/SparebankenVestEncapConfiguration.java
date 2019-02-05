package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapConfiguration;

public class SparebankenVestEncapConfiguration implements EncapConfiguration {

    @Override
    public String getApplicationVersion() {
        return "3.2.4";
    }

    @Override
    public String getEncapApiVersion() {
        return "3.3.5";
    }

    @Override
    public String getCredentialsAppNameForEdb() {
        return "SPV_MOBILE_BANKING";
    }

    @Override
    public String getCredentialsBankCodeForEdb() {
        return "(null)";
    }

    @Override
    public String getSaIdentifier() {
        return "samobile_spv_mobilebank_android_v1";
    }

    @Override
    public String getAppId() {
        return "no.spv.mobilbank";
    }

    @Override
    public String getRsaPubKeyString() {
        return "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3maOiHUOhZR75rlXiyic"
                + "csi5mp5OEdkamnC1oRO1o71eP2u7v3i3sEIHQ9jHaIw6kHCrHqFCPvgjvbzcM8vC"
                + "uHZF3xafYCxShUH6Kb5AU7of6L7dTqXJDwyK6EJ1sGX1qIrlqVdYzDtfEES7NZb4"
                + "nJOpcFzeG9Nt9N7slm4Xq7KFYHFSkVXOWF2Se9f/raoaYVkFCNK8XClw1wPRnkc0"
                + "587xE1qwUa661m/pmCkm6M0FO7wfdS9zOQuq9Ual1x2sD7q+H2UhKtmY9zb31paM"
                + "ZDa6Tr3/eHopfisV/g1LxeVx/99tVf7b3vdAbBlcBep6YaawnhWM27NGEZ/jldzK"
                + "YQIDAQAB";
    }

    @Override
    public String getClientPrivateKeyString() {
        return "MIIEpQIBAAKCAQEAxDxXJ87syyV81f84LkxrQT8mZDDX4wFufkaXD6a2QudGR2S2"
                + "1SMy/atrEWofwIOVHJ7hjbqCR3Sch3RRwa6T9BBA43wTa6uzSsx9N8ACw8DCSn/g"
                + "8SyqtrXECQH7k7dVtqkkpWdLiLYq+bzLTNIS9wCvxyWXS6RCGZFlJg6M/jrkAI7L"
                + "/csGq58brzsVFbDEzWL3WGxSFxXm/3Z3GIRnGvcPP+VXFgmIr49MX3lzsXDKs7ej"
                + "++KbZAs8TtRvB+hdTJvnA4iUzaeROMTXApZ0fii7xTTzpnLIjVHB5vcFZtRlBbJ/"
                + "pEFIEbcCJOaOQpeyLktkkxTFJcj0mMi5k0KuEQIDAQABAoIBAQCTijKVMZUDn+ne"
                + "E4ev97DkW5CH39lRSfBDbNuC3vtGDhDe+LAmS9K4urtDWqEI42WnISXAo6v0AY72"
                + "oNQfZRzEcbv3zX/kgjHJWToEQo9RkooB7We4rLalrL/5bc4y37LIJR/yZFl8Lkrd"
                + "fb70wTWVFKUUfATm0FcKVjDQjjwfn+BiZ/Dwy05IYCjBA8wHN/JKCLyfaMHtxz7S"
                + "T/aB4oBIR4YhRUteP1RK96vp39TfDkOhNbCR80Pbdu4HYflvaol2PiQhnWsXSuMw"
                + "Zk4EVkA4gbQKyQToGFmmL2EPczVBP+bjyEOhHtkDxWOtXudUf2LswzoTcj/q8OB2"
                + "DeVjLH6pAoGBAPoQHaRoDKAwYBmm8ZKbBR2vdh8KWkMVGrigewO2viZBuYWV7mJ0"
                + "3crtRlW1qTNYNGeCbfQOpzgcGKuHIHbsjHEK4sl24VUcZyRRzGfkcX7LMAP9i86/"
                + "t8Ql0uKwhdh7TWgtrUmhi9ejkNOzg8+zjFk5LBX/yGGDsCKOsU5wyLl3AoGBAMjl"
                + "D/ON0xqOlMyt9Nbjrktti41AO61x7SZ8lO4ZVZre/3iOcTYYLzAuhD0oSuQYAaeZ"
                + "MJLYMPonG7dOzh5mgYsuXfGmNPIMTpiW1Ahb6q9RWYQ1HNkgD5RYpu7DdIyA2TsQ"
                + "54b9aCwDdsP5W5IutPCh4VTWa/hp+MHgdBECUTa3AoGBAPdICIgT/KhMLjwvwqPf"
                + "eGeqo3x+mFPOAuagjAVYKSFbzUI0yc/DEbEignMveWq0xt21NVYrR2FNJ3g33/q6"
                + "YQdIZIwlax7nP3fDDAeQ+EsSUyEfEaoaHHdzj99sAG/bPujOgpZVkhxkO0ueMbKr"
                + "MxboWphGoLkNqJgD04JmNZhxAoGBALd3EbFAhVkMe3HNL/uLpKI34alWzulgUM7E"
                + "GZK0SkyMeMzVcSiQv4+F+7iKcpKKJgC0UvrYOypIyvQGIRD8VjQQXTURr+qZOLH7"
                + "lJopr1L6vQCTLMxRjkjRCWqlz2t9RGW/02GhTSBc7Xqd4HXI9++GNu2ugJ410Trl"
                + "y7m43whTAoGASJRMw4aNIVhdW/KfyJ0ktLEOvnLU8877JNctMaaI292xmcE6jL1Z"
                + "bOO2JGuMAX2EBv2pFumecdzEgfV/O7wZpEcGiLmHRCHnfv8ANm+aTSFJLrUqhxO0"
                + "mYvrf2MS8j7o+3lSMXrL6fNU9rJdJin+wP22/xyUh2yEAmWoQBUtRUY=";
    }
}
