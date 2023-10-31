package org.hyperledger.besu.ethereum.trie.verkle;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * A class responsible for hashing an array of Bytes32 using the SHA-256 (Secure Hash Algorithm 256-bit) hashing algorithm.
 *
 * <p>This class implements the Hasher interface and provides a method to commit multiple Bytes32 inputs using the SHA-256 hashing algorithm.
 * It utilizes the Java built-in MessageDigest for SHA-256.
 */
public class SHA256Hasher implements Hasher<Bytes32> {

    /**
     * Commits an array of Bytes32 using the SHA-256 hashing algorithm provided by the MessageDigest.
     *
     * @param inputs An array of Bytes32 inputs to be hashed and committed.
     * @return The resulting hash as a Bytes32.
     */
    @Override
    public Bytes32 commit(Bytes32[] inputs) {
        Bytes32 out;
        Bytes inputs_serialized = Bytes.concatenate(inputs);
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            out = Bytes32.wrap(sha256.digest(inputs_serialized.toArray()));
        } catch (NoSuchAlgorithmException e) {
            out = Bytes32.ZERO;
        }
        return out;
    }
}
