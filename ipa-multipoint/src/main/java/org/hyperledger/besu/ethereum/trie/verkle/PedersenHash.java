package org.hyperledger.besu.ethereum.trie.verkle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.nativelib.ipamultipoint.LibIpaMultipoint;


public class PedersenHash {
    public void pedersenHash() {}

    public Bytes32 digest(Bytes input) {
        int size = input.size();
        if (size > 255 * 16) {
            throw new IllegalArgumentException("Pedersen Hash's input is too long (>255*16 bytes)");
        }
        byte[][] inp = new byte[256][];  // input to nativelib
        int header = 2 + 255 * size;
        inp[0] = Bytes.ofUnsignedInt(header).toArray();

        // 0-padded input of 255 chunks of 16 bytes 
        byte[] paddedInput = Arrays.copyOf(input.toArray(), 255 * 16);
        for (int i = 0; i < 255; i++) {
            inp[i + 1] = Arrays.copyOfRange(paddedInput, 16 * i, 16 * (i + 1));
        }
        // Once commit is fixed, we need to use it. For now, using sha256
        // Bytes32 out = Bytes32.wrap(LibIpaMultipoint.commit(inp));
        Bytes32 out;
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            out = Bytes32.wrap(sha256.digest(paddedInput));
        } catch (NoSuchAlgorithmException e) {
            out = Bytes32.ZERO;
        }
        return out;
    } 
}
