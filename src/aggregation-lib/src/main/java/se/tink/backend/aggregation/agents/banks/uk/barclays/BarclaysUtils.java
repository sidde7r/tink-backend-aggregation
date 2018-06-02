package se.tink.backend.aggregation.agents.banks.uk.barclays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Bytes;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Request;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.Response;
import se.tink.backend.aggregation.agents.banks.uk.barclays.rpc.ServiceMessage;

public class BarclaysUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int REQUEST_UNAUTH_HEADER_LENGTH = 0x46;
    private static final int REQUEST_AUTH_HEADER_LENGTH = 0x34;
    private static final int RESPONSE_HEADER_LENGTH = 0x29;
    private static final int HEADER_XOR_KEY_LENGTH = 16;

    private static byte[] lengthToBigEndian(int len) {
        return new byte[] {
                (byte)((len >> 24) & 0xff),
                (byte)((len >> 16) & 0xff),
                (byte)((len >> 8) & 0xff),
                (byte)(len & 0xff)
        };
    }

    private static byte[] xorHeader(byte[] header) {
        // xor everything in the header except the key part
        for (int i=HEADER_XOR_KEY_LENGTH; i<header.length; i++) {
            header[i] ^= header[i%HEADER_XOR_KEY_LENGTH];
        }
        return header;
    }

    public static byte[] serializeRequest(BarclaysSession session, Request request) {
        /*
            header format (len: 0x34/52 or 0x46/70)
                16b     xor key / obfuscation key
                16b     session id
                16b     encrypted iv ("decrypted")
                4b      BE data length
                (1b      protocol version)
                (3b      platform identifier)
                (9b      application version)
                (5b      command id)
         */
        /*
            body format
                (Yb     RSA encrypted session seed)
                Xb      AES 256 GCM encrypted request body
         */
        byte[] xorKey = BarclaysCrypto.random(HEADER_XOR_KEY_LENGTH);
        byte[] iv = BarclaysCrypto.random(16);
        byte[] encryptedIv = BarclaysCrypto.aesEcbDecrypt(session.getCliIvKey(), iv);
        byte[] rsaEncryptedSessionSeed = new byte[0]; // empty

        String commandId = request.getCommandId();
        Preconditions.checkState(commandId.length() == 5, "Command id too short: %s", commandId);

        Map<String, String> commandBodyElements = request.getBody();
        if (session.isAuthenticated()) {
            // Authenticated sessions have the command id in the body
            Preconditions.checkState(
                            !commandBodyElements.containsKey("opCode"),
                            "Command id (%s) defined in body",
                            commandId);
            commandBodyElements.put("opCode", commandId);
        }
        String commandBody = Joiner.on("\n").withKeyValueSeparator("=").join(commandBodyElements) + "\n";

        int headerLength = session.isAuthenticated() ? REQUEST_AUTH_HEADER_LENGTH : REQUEST_UNAUTH_HEADER_LENGTH;
        int requestLength = headerLength + commandBody.length() + BarclaysCrypto.getAesGcmTagLength();
        if (!session.hasSessionId()) {
            // No session id assigned yet: prepend RSA encrypted session seed to the body
            rsaEncryptedSessionSeed = BarclaysCrypto.rsaOaepEncrypt(BarclaysConstants.RSA_PUB_KEY, session.getSeed());
            requestLength += rsaEncryptedSessionSeed.length;
        }

        byte[] header = Bytes.concat(
                                xorKey,
                                session.getSessionId().getBytes(Charsets.UTF_8),
                                encryptedIv,
                                lengthToBigEndian(requestLength));
        if (!session.isAuthenticated()) {
            // unauthenticated request have additional fields in the header
            header = Bytes.concat(
                                header,
                                BarclaysConstants.PROTOCOL_VERSION,
                                BarclaysConstants.PLATFORM_ID.getBytes(Charsets.UTF_8),
                                BarclaysConstants.APPLICATION_VERSION.getBytes(Charsets.UTF_8),
                                commandId.getBytes(Charsets.UTF_8));
        }
        Preconditions.checkState(
                        header.length == headerLength,
                        "Header has invalid size, command id: %s",
                        commandId);

        byte[] encryptedBody = BarclaysCrypto.aesGcmEncrypt(
                                                        session.getCliAesKey(),
                                                        iv,
                                                        header,
                                                        commandBody.getBytes(Charsets.UTF_8));
        byte[] xoredHeader = xorHeader(header);
        return Bytes.concat(xoredHeader, rsaEncryptedSessionSeed, encryptedBody);
    }

    public static <T extends Response> T deserializeResponse(BarclaysSession session, byte[] data, Class<T> resType) {
        /*
            header format (len: 0x29/41)
                16b     xor key / obfuscation key
                5b      status code ("00000" == success)
                16b     encrypted iv
                4b      BE data length
         */
        /*
            body format
                Xb      AES 256 GCM encrypted response body
         */

        // When the service is down, for e.g. maintenance, the response data will be a JSON encoded message.
        // I.e. it will not be encrypted in any way. Service windows are often on Sundays (mid day).
        if (isByteArrayAscii(data)) {
            ServiceMessage svcMessage = new ServiceMessage();
            try {
                MAPPER.readerForUpdating(svcMessage).readValue(data);
                // todo: handle this better
                throw new IllegalStateException(svcMessage.getSvc_msg());
            } catch(IOException e) {
                // do nothing, it wasn't a ServiceMessage
            }
        }

        Preconditions.checkArgument(data.length >= RESPONSE_HEADER_LENGTH);

        byte[] xoredHeader = Arrays.copyOfRange(data, 0, RESPONSE_HEADER_LENGTH);
        byte[] encryptedBody = Arrays.copyOfRange(data, RESPONSE_HEADER_LENGTH, data.length);
        byte[] header = xorHeader(xoredHeader);

        String statusCode = new String(Arrays.copyOfRange(header, HEADER_XOR_KEY_LENGTH, HEADER_XOR_KEY_LENGTH+5));
        switch(statusCode) {
        case "00000":
            // successful
            break;
        case "00103":
            // Generic crypto error.
            // Could mean (based on tests): invalid RSA encryption, invalid AES key, invalid AES IV or invalid GCM tag.
            // This error results in a "Temporary error" prompt in the client.
            throw new IllegalStateException("Generic crypto error response (103)");
        default:
            throw new IllegalStateException(String.format("Unknown response status code: %s", statusCode));
        }

        int responseLength = 0;
        responseLength |= (header[37] & 0xFF) << 24;
        responseLength |= (header[37+1] & 0xFF) << 16;
        responseLength |= (header[37+2] & 0xFF) << 8;
        responseLength |= (header[37+3] & 0xFF);
        Preconditions.checkState(
                data.length == responseLength,
                "Invalid response body length. Expected: %d, found: %d.",
                data.length,
                responseLength);

        byte[] encryptedIv = Arrays.copyOfRange(header, 21, 21+16);
        byte[] iv = BarclaysCrypto.aesEcbEncrypt(session.getSrvIvKey(), encryptedIv);
        byte[] body = BarclaysCrypto.aesGcmDecrypt(session.getSrvAesKey(), iv, header, encryptedBody);

        T resModel;
        try {
            resModel = (T) resType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        // calculate the hash of the original request data, it's needed in some of the auth steps
        byte[] dataHash = BarclaysCrypto.sha256(data);
        resModel.set__dataHash__(dataHash);

        try {
            MAPPER.readerForUpdating(resModel).readValue(body);
            return resModel;
        } catch(IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static byte[] generateDummySignature() {
        // Taken directly from the executable.
        // The purpose is to format it as an asn1 encoded signature but with dummy/random values. No idea why.
        byte[] hdr = new byte[] {
                (byte)0x30, (byte)0x44
        };
        byte[] delim = new byte[] {
                (byte)0x02, (byte)0x20,
        };
        byte[] r0 = BarclaysCrypto.random(32);
        byte[] r1 = BarclaysCrypto.random(32);
        return Bytes.concat(hdr, delim, r0, delim, r1);
    }

    private static boolean isByteArrayAscii(byte[] barray) {
        for (byte a : barray) {
            if ((a & 0xff) > 127) {
                return false;
            }
        }
        return true;
    }
}
