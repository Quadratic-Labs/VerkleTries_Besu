package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.Bytes;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EmptyNode implements VerkleNode{
    int depth;
    Bytes commitment;

    public EmptyNode() {
        this(0);
        setCommitment(Bytes32.ZERO);
    }

    public EmptyNode(int depth) {
        this.depth = depth;
    }

    public Bytes getCommitment() {
        return commitment;
    }

    public void setCommitment() throws Exception {
        setCommitment(Bytes32.ZERO);
    }

    public void setCommitment(Bytes commitment) {
        this.commitment = commitment;
    }

    public void insert(Bytes32 key, Bytes32 value) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }
}



public class PedersenHash {
    private static final String HASH_ALGORITHM = "SHA-256";

    public static byte[] pedersenHash(byte[] bytes64) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] result = new byte[32];

        byte[][] chunks = chunk64(bytes64);

        for (int i = 0; i < chunks.length; i++) {
            byte[] chunk = chunks[i];

            // Reverse the endian of the chunk
            byte[] reversedChunk = reverseEndian(chunk);

            // Compute the hash of the reversed chunk
            byte[] hash = digest.digest(reversedChunk);

            // Accumulate the results by XORing with the previous results
            xor(result, hash);
        }

        return result;
    }

    private static byte[][] chunk64(byte[] data) {
        int chunks = (data.length + 63) / 64;
        byte[][] result = new byte[chunks][64];
        int offset = 0;

        for (int i = 0; i < chunks; i++) {
            int length = Math.min(64, data.length - offset);
            result[i] = new byte[64];
            System.arraycopy(data, offset, result[i], 0, length);
            offset += length;
        }

        return result;
    }

    private static byte[] reverseEndian(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversed[i] = bytes[bytes.length - 1 - i];
        }
        return reversed;
    }

    private static void xor(byte[] result, byte[] hash) {
        for (int i = 0; i < result.length; i++) {
            result[i] ^= hash[i];
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


}


