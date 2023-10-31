package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.nativelib.ipamultipoint.LibIpaMultipoint;

/**
 * A class responsible for hashing an array of Bytes32 using the IPA (Inter-Participant Agreement) hashing algorithm.
 *
 * <p>This class implements the Hasher interface and provides a method to commit multiple Bytes32 inputs using the IPA hashing algorithm.
 */
public class IPAHasher implements Hasher<Bytes32> {

    /**
     * Commits an array of Bytes32 using the IPA hashing algorithm.
     *
     * @param inputs An array of Bytes32 inputs to be hashed and committed.
     * @return The resulting hash as a Bytes32.
     */
    @Override
    public Bytes32 commit(Bytes32[] inputs) {
        Bytes input_serialized = Bytes.concatenate(inputs);
        return Bytes32.wrap(LibIpaMultipoint.commit(input_serialized.toArray()));
    }
}
