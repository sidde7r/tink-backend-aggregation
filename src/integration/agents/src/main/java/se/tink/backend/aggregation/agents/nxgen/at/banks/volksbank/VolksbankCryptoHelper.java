package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank;

import java.security.interfaces.RSAPublicKey;
import java.util.Random;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.codec.binary.Hex;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public final class VolksbankCryptoHelper {

    private static final int PUSH_TOKEN_LENGTH = 32;
    private static final Random RANDOM = new Random();

    // jsSHA: https://github.com/Caligatio/jsSHA/blob/master/src/sha1.js
    private static final String js =
            "var Totp = function hash(secret) {\n"
                    + "    var shaObj = new jsSHA(\"SHA-1\", \"HEX\");\n"
                    + "    shaObj.setHMACKey(base32tohex(secret), \"HEX\");\n"
                    + "    shaObj.update(leftpad(dec2hex(currentInterval()), 16, '0'));\n"
                    + "    var hmac = shaObj.getHMAC(\"HEX\");\n"
                    + "    var offset = hex2dec(hmac.substring(hmac.length - 1));\n"
                    + "    var otp = (hex2dec(hmac.substr(offset * 2, 8)) & hex2dec('7fffffff')) + '';\n"
                    + "    otp = (otp).substr(otp.length - 6, 6);\n"
                    + "\n"
                    + "    return otp;\n"
                    + "}\n"
                    + "function dec2hex(s) { return (s < 15.5 ? '0' : '') + Math.round(s).toString(16); }\n"
                    + "function hex2dec(s) { return parseInt(s, 16); }\n"
                    + "\n"
                    + "function base32tohex(base32) {\n"
                    + "    var base32chars = \"ABCDEFGHIJKLMNOPQRSTUVWXYZ234567\";\n"
                    + "    var bits = \"\";\n"
                    + "    var hex = \"\";\n"
                    + "\n"
                    + "    for (var i = 0; i < base32.length; i++) {\n"
                    + "        var val = base32chars.indexOf(base32.charAt(i).toUpperCase());\n"
                    + "        bits += leftpad(val.toString(2), 5, '0');\n"
                    + "    }\n"
                    + "\n"
                    + "    for (var i = 0; i+4 <= bits.length; i+=4) {\n"
                    + "        var chunk = bits.substr(i, 4);\n"
                    + "        hex = hex + parseInt(chunk, 2).toString(16) ;\n"
                    + "    }\n"
                    + "    return hex;\n"
                    + "}\n"
                    + "\n"
                    + "function leftpad(str, len, pad) {\n"
                    + "    if (len + 1 >= str.length) {\n"
                    + "        str = Array(len + 1 - str.length).join(pad) + str;\n"
                    + "    }\n"
                    + "        return str;\n"
                    + "    }\n"
                    + "\n"
                    + "function currentInterval() {\n"
                    + "    return Math.floor((new Date()).getTime() / 30000);\n"
                    + "}\n"
                    + "\n"
                    + "'use strict';\n"
                    + "(function(E) {\n"
                    + "    function t(c, a, e) {\n"
                    + "        var g = 0,\n"
                    + "            b = [],\n"
                    + "            d = 0,\n"
                    + "            f, k, l, h, m, w, n, q = !1,\n"
                    + "            r = !1,\n"
                    + "            p = [],\n"
                    + "            t = [],\n"
                    + "            v, u = !1;\n"
                    + "        e = e || {};\n"
                    + "        f = e.encoding || \"UTF8\";\n"
                    + "        v = e.numRounds || 1;\n"
                    + "        l = y(a, f);\n"
                    + "        if (v !== parseInt(v, 10) || 1 > v) throw Error(\"numRounds must a integer >= 1\");\n"
                    + "        if (\"SHA-1\" === c) m = 512, w = z, n = F, h = 160;\n"
                    + "        else throw Error(\"Chosen SHA variant is not supported\");\n"
                    + "        k = x(c);\n"
                    + "        this.setHMACKey = function(a, b, d) {\n"
                    + "            var e;\n"
                    + "            if (!0 === r) throw Error(\"HMAC key already set\");\n"
                    + "            if (!0 === q) throw Error(\"Cannot set HMAC key after finalizing hash\");\n"
                    + "            if (!0 === u) throw Error(\"Cannot set HMAC key after calling update\");\n"
                    + "            f = (d || {}).encoding || \"UTF8\";\n"
                    + "            b = y(b, f)(a);\n"
                    + "            a = b.binLen;\n"
                    + "            b = b.value;\n"
                    + "            e = m >>> 3;\n"
                    + "            d = e / 4 - 1;\n"
                    + "            if (e < a / 8) {\n"
                    + "                for (b = n(b, a, 0, x(c)); b.length <= d;) b.push(0);\n"
                    + "                b[d] &= 4294967040\n"
                    + "            } else if (e > a / 8) {\n"
                    + "                for (; b.length <= d;) b.push(0);\n"
                    + "                b[d] &= 4294967040\n"
                    + "            }\n"
                    + "            for (a = 0; a <= d; a += 1) p[a] = b[a] ^ 909522486, t[a] = b[a] ^ 1549556828;\n"
                    + "            k = w(p, k);\n"
                    + "            g = m;\n"
                    + "            r = !0\n"
                    + "        };\n"
                    + "        this.update = function(a) {\n"
                    + "            var c, e, f, h = 0,\n"
                    + "                n = m >>> 5;\n"
                    + "            c = l(a, b, d);\n"
                    + "            a = c.binLen;\n"
                    + "            e = c.value;\n"
                    + "            c = a >>> 5;\n"
                    + "            for (f = 0; f < c; f += n) h + m <= a && (k = w(e.slice(f, f + n), k), h += m);\n"
                    + "            g += h;\n"
                    + "            b = e.slice(h >>> 5);\n"
                    + "            d = a % m;\n"
                    + "            u = !0\n"
                    + "        };\n"
                    + "        this.getHash = function(a, e) {\n"
                    + "            var f, l, m;\n"
                    + "            if (!0 ===\n"
                    + "                r) throw Error(\"Cannot call getHash after setting HMAC key\");\n"
                    + "            m = A(e);\n"
                    + "            switch (a) {\n"
                    + "                case \"HEX\":\n"
                    + "                    f = function(a) {\n"
                    + "                        return B(a, m)\n"
                    + "                    };\n"
                    + "                    break;\n"
                    + "                case \"B64\":\n"
                    + "                    f = function(a) {\n"
                    + "                        return C(a, m)\n"
                    + "                    };\n"
                    + "                    break;\n"
                    + "                case \"BYTES\":\n"
                    + "                    f = D;\n"
                    + "                    break;\n"
                    + "                default:\n"
                    + "                    throw Error(\"format must be HEX, B64, or BYTES\");\n"
                    + "            }\n"
                    + "            if (!1 === q)\n"
                    + "                for (k = n(b, d, g, k), l = 1; l < v; l += 1) k = n(k, h, 0, x(c));\n"
                    + "            q = !0;\n"
                    + "            return f(k)\n"
                    + "        };\n"
                    + "        this.getHMAC = function(a, e) {\n"
                    + "            var f, l, p;\n"
                    + "            if (!1 === r) throw Error(\"Cannot call getHMAC without first setting HMAC key\");\n"
                    + "            p = A(e);\n"
                    + "            switch (a) {\n"
                    + "                case \"HEX\":\n"
                    + "                    f = function(a) {\n"
                    + "                        return B(a, p)\n"
                    + "                    };\n"
                    + "                    break;\n"
                    + "                case \"B64\":\n"
                    + "                    f =\n"
                    + "                        function(a) {\n"
                    + "                            return C(a, p)\n"
                    + "                        };\n"
                    + "                    break;\n"
                    + "                case \"BYTES\":\n"
                    + "                    f = D;\n"
                    + "                    break;\n"
                    + "                default:\n"
                    + "                    throw Error(\"outputFormat must be HEX, B64, or BYTES\");\n"
                    + "            }!1 === q && (l = n(b, d, g, k), k = w(t, x(c)), k = n(l, h, m, k));\n"
                    + "            q = !0;\n"
                    + "            return f(k)\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    function G(c, a, e) {\n"
                    + "        var g = c.length,\n"
                    + "            b, d, f, k, l;\n"
                    + "        a = a || [0];\n"
                    + "        e = e || 0;\n"
                    + "        l = e >>> 3;\n"
                    + "        if (0 !== g % 2) throw Error(\"String of HEX type must be in byte increments\");\n"
                    + "        for (b = 0; b < g; b += 2) {\n"
                    + "            d = parseInt(c.substr(b, 2), 16);\n"
                    + "            if (isNaN(d)) throw Error(\"String of HEX type contains invalid characters\");\n"
                    + "            k = (b >>> 1) + l;\n"
                    + "            for (f = k >>> 2; a.length <= f;) a.push(0);\n"
                    + "            a[f] |= d <<\n"
                    + "                8 * (3 - k % 4)\n"
                    + "        }\n"
                    + "        return {\n"
                    + "            value: a,\n"
                    + "            binLen: 4 * g + e\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    function H(c, a, e) {\n"
                    + "        var g = [],\n"
                    + "            b, d, f, k, g = a || [0];\n"
                    + "        e = e || 0;\n"
                    + "        d = e >>> 3;\n"
                    + "        for (b = 0; b < c.length; b += 1) a = c.charCodeAt(b), k = b + d, f = k >>> 2, g.length <= f && g.push(0), g[f] |= a << 8 * (3 - k % 4);\n"
                    + "        return {\n"
                    + "            value: g,\n"
                    + "            binLen: 8 * c.length + e\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    function I(c, a, e) {\n"
                    + "        var g = [],\n"
                    + "            b = 0,\n"
                    + "            d, f, k, l, h, m, g = a || [0];\n"
                    + "        e = e || 0;\n"
                    + "        a = e >>> 3;\n"
                    + "        if (-1 === c.search(/^[a-zA-Z0-9=+\\/]+$/)) throw Error(\"Invalid character in base-64 string\");\n"
                    + "        f = c.indexOf(\"=\");\n"
                    + "        c = c.replace(/\\=/g, \"\");\n"
                    + "        if (-1 !== f && f < c.length) throw Error(\"Invalid '=' found in base-64 string\");\n"
                    + "        for (f = 0; f < c.length; f += 4) {\n"
                    + "            h = c.substr(f, 4);\n"
                    + "            for (k = l = 0; k < h.length; k += 1) d = \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\".indexOf(h[k]), l |= d << 18 - 6 * k;\n"
                    + "            for (k = 0; k < h.length - 1; k += 1) {\n"
                    + "                m = b + a;\n"
                    + "                for (d = m >>> 2; g.length <= d;) g.push(0);\n"
                    + "                g[d] |= (l >>> 16 - 8 * k & 255) << 8 * (3 - m % 4);\n"
                    + "                b += 1\n"
                    + "            }\n"
                    + "        }\n"
                    + "        return {\n"
                    + "            value: g,\n"
                    + "            binLen: 8 * b + e\n"
                    + "        }\n"
                    + "    }\n"
                    + "\n"
                    + "    function B(c, a) {\n"
                    + "        var e = \"\",\n"
                    + "            g = 4 * c.length,\n"
                    + "            b, d;\n"
                    + "        for (b = 0; b < g; b += 1) d = c[b >>> 2] >>> 8 * (3 - b % 4), e += \"0123456789abcdef\".charAt(d >>> 4 & 15) + \"0123456789abcdef\".charAt(d & 15);\n"
                    + "        return a.outputUpper ? e.toUpperCase() : e\n"
                    + "    }\n"
                    + "\n"
                    + "    function C(c,\n"
                    + "        a) {\n"
                    + "        var e = \"\",\n"
                    + "            g = 4 * c.length,\n"
                    + "            b, d, f;\n"
                    + "        for (b = 0; b < g; b += 3)\n"
                    + "            for (f = b + 1 >>> 2, d = c.length <= f ? 0 : c[f], f = b + 2 >>> 2, f = c.length <= f ? 0 : c[f], f = (c[b >>> 2] >>> 8 * (3 - b % 4) & 255) << 16 | (d >>> 8 * (3 - (b + 1) % 4) & 255) << 8 | f >>> 8 * (3 - (b + 2) % 4) & 255, d = 0; 4 > d; d += 1) 8 * b + 6 * d <= 32 * c.length ? e += \"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/\".charAt(f >>> 6 * (3 - d) & 63) : e += a.b64Pad;\n"
                    + "        return e\n"
                    + "    }\n"
                    + "\n"
                    + "    function D(c) {\n"
                    + "        var a = \"\",\n"
                    + "            e = 4 * c.length,\n"
                    + "            g, b;\n"
                    + "        for (g = 0; g < e; g += 1) b = c[g >>> 2] >>> 8 * (3 - g % 4) & 255, a += String.fromCharCode(b);\n"
                    + "        return a\n"
                    + "    }\n"
                    + "\n"
                    + "    function A(c) {\n"
                    + "        var a = {\n"
                    + "            outputUpper: !1,\n"
                    + "            b64Pad: \"=\"\n"
                    + "        };\n"
                    + "        c = c || {};\n"
                    + "        a.outputUpper = c.outputUpper || !1;\n"
                    + "        a.b64Pad = c.b64Pad || \"=\";\n"
                    + "        if (\"boolean\" !== typeof a.outputUpper) throw Error(\"Invalid outputUpper formatting option\");\n"
                    + "        if (\"string\" !== typeof a.b64Pad) throw Error(\"Invalid b64Pad formatting option\");\n"
                    + "        return a\n"
                    + "    }\n"
                    + "\n"
                    + "    function y(c, a) {\n"
                    + "        var e;\n"
                    + "        switch (a) {\n"
                    + "            case \"UTF8\":\n"
                    + "            case \"UTF16BE\":\n"
                    + "            case \"UTF16LE\":\n"
                    + "                break;\n"
                    + "            default:\n"
                    + "                throw Error(\"encoding must be UTF8, UTF16BE, or UTF16LE\");\n"
                    + "        }\n"
                    + "        switch (c) {\n"
                    + "            case \"HEX\":\n"
                    + "                e = G;\n"
                    + "                break;\n"
                    + "            case \"TEXT\":\n"
                    + "                e = function(e, b, d) {\n"
                    + "                    var f = [],\n"
                    + "                        c = [],\n"
                    + "                        l = 0,\n"
                    + "                        h, m, p, n, q, f = b || [0];\n"
                    + "                    b = d || 0;\n"
                    + "                    p = b >>> 3;\n"
                    + "                    if (\"UTF8\" ===\n"
                    + "                        a)\n"
                    + "                        for (h = 0; h < e.length; h += 1)\n"
                    + "                            for (d = e.charCodeAt(h), c = [], 128 > d ? c.push(d) : 2048 > d ? (c.push(192 | d >>> 6), c.push(128 | d & 63)) : 55296 > d || 57344 <= d ? c.push(224 | d >>> 12, 128 | d >>> 6 & 63, 128 | d & 63) : (h += 1, d = 65536 + ((d & 1023) << 10 | e.charCodeAt(h) & 1023), c.push(240 | d >>> 18, 128 | d >>> 12 & 63, 128 | d >>> 6 & 63, 128 | d & 63)), m = 0; m < c.length; m += 1) {\n"
                    + "                                q = l + p;\n"
                    + "                                for (n = q >>> 2; f.length <= n;) f.push(0);\n"
                    + "                                f[n] |= c[m] << 8 * (3 - q % 4);\n"
                    + "                                l += 1\n"
                    + "                            } else if (\"UTF16BE\" === a || \"UTF16LE\" === a)\n"
                    + "                                for (h = 0; h < e.length; h += 1) {\n"
                    + "                                    d = e.charCodeAt(h);\n"
                    + "                                    \"UTF16LE\" === a && (m = d & 255, d = m << 8 | d >>> 8);\n"
                    + "                                    q = l + p;\n"
                    + "                                    for (n = q >>>\n"
                    + "                                        2; f.length <= n;) f.push(0);\n"
                    + "                                    f[n] |= d << 8 * (2 - q % 4);\n"
                    + "                                    l += 2\n"
                    + "                                }\n"
                    + "                    return {\n"
                    + "                        value: f,\n"
                    + "                        binLen: 8 * l + b\n"
                    + "                    }\n"
                    + "                };\n"
                    + "                break;\n"
                    + "            case \"B64\":\n"
                    + "                e = I;\n"
                    + "                break;\n"
                    + "            case \"BYTES\":\n"
                    + "                e = H;\n"
                    + "                break;\n"
                    + "            default:\n"
                    + "                throw Error(\"format must be HEX, TEXT, B64, or BYTES\");\n"
                    + "        }\n"
                    + "        return e\n"
                    + "    }\n"
                    + "\n"
                    + "    function r(c, a) {\n"
                    + "        return c << a | c >>> 32 - a\n"
                    + "    }\n"
                    + "\n"
                    + "    function p(c, a) {\n"
                    + "        var e = (c & 65535) + (a & 65535);\n"
                    + "        return ((c >>> 16) + (a >>> 16) + (e >>> 16) & 65535) << 16 | e & 65535\n"
                    + "    }\n"
                    + "\n"
                    + "    function u(c, a, e, g, b) {\n"
                    + "        var d = (c & 65535) + (a & 65535) + (e & 65535) + (g & 65535) + (b & 65535);\n"
                    + "        return ((c >>> 16) + (a >>> 16) + (e >>> 16) + (g >>> 16) + (b >>> 16) + (d >>> 16) & 65535) << 16 | d & 65535\n"
                    + "    }\n"
                    + "\n"
                    + "    function x(c) {\n"
                    + "        if (\"SHA-1\" ===\n"
                    + "            c) c = [1732584193, 4023233417, 2562383102, 271733878, 3285377520];\n"
                    + "        else throw Error(\"No SHA variants supported\");\n"
                    + "        return c\n"
                    + "    }\n"
                    + "\n"
                    + "    function z(c, a) {\n"
                    + "        var e = [],\n"
                    + "            g, b, d, f, k, l, h;\n"
                    + "        g = a[0];\n"
                    + "        b = a[1];\n"
                    + "        d = a[2];\n"
                    + "        f = a[3];\n"
                    + "        k = a[4];\n"
                    + "        for (h = 0; 80 > h; h += 1) e[h] = 16 > h ? c[h] : r(e[h - 3] ^ e[h - 8] ^ e[h - 14] ^ e[h - 16], 1), l = 20 > h ? u(r(g, 5), b & d ^ ~b & f, k, 1518500249, e[h]) : 40 > h ? u(r(g, 5), b ^ d ^ f, k, 1859775393, e[h]) : 60 > h ? u(r(g, 5), b & d ^ b & f ^ d & f, k, 2400959708, e[h]) : u(r(g, 5), b ^ d ^ f, k, 3395469782, e[h]), k = f, f = d, d = r(b, 30), b = g, g = l;\n"
                    + "        a[0] = p(g, a[0]);\n"
                    + "        a[1] = p(b, a[1]);\n"
                    + "        a[2] = p(d, a[2]);\n"
                    + "        a[3] = p(f, a[3]);\n"
                    + "        a[4] = p(k, a[4]);\n"
                    + "        return a\n"
                    + "    }\n"
                    + "\n"
                    + "    function F(c, a, e, g) {\n"
                    + "        var b;\n"
                    + "        for (b = (a + 65 >>> 9 << 4) + 15; c.length <= b;) c.push(0);\n"
                    + "        c[a >>> 5] |= 128 << 24 - a % 32;\n"
                    + "        c[b] = a + e;\n"
                    + "        e = c.length;\n"
                    + "        for (a = 0; a < e; a += 16) g = z(c.slice(a, a + 16), g);\n"
                    + "        return g\n"
                    + "    }\n"
                    + "    \"function\" === typeof define && define.amd ? define(function() {\n"
                    + "        return t\n"
                    + "    }) : \"undefined\" !== typeof exports ? \"undefined\" !== typeof module && module.exports ? module.exports = exports = t : exports = t : E.jsSHA = t\n"
                    + "})(this);";

    // Hide Ctor
    private VolksbankCryptoHelper() {
        throw new AssertionError();
    }

    static String generateRandomHex() {
        byte[] randBytes = new byte[PUSH_TOKEN_LENGTH];
        RANDOM.nextBytes(randBytes);
        return Hex.encodeHexString(randBytes).toUpperCase();
    }

    static String encryptPin(String pin) {
        return EncodingUtils.encodeHexAsString(
                RSA.encryptNonePkcs1(
                        generateRSAPublicKey(),
                        (VolksbankConstants.Crypto.RSA_SALT + pin).getBytes()));
    }

    private static RSAPublicKey generateRSAPublicKey() {
        byte[] modulusBytes = EncodingUtils.decodeHexString(VolksbankConstants.Crypto.RSA_MODULUS);
        byte[] exponentBytes =
                EncodingUtils.decodeHexString(VolksbankConstants.Crypto.RSA_EXPONENT);

        return RSA.getPublicKeyFromModulusAndExponent(modulusBytes, exponentBytes);
    }

    static String getTotp(String secret) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            engine.eval(js);

            Invocable inv = (Invocable) engine;
            return (String) inv.invokeFunction(VolksbankConstants.Crypto.OTP_ALGO, secret);
        } catch (NoSuchMethodException | ScriptException e) {
            throw new IllegalStateException("javascript parsing failed", e);
        }
    }
}
