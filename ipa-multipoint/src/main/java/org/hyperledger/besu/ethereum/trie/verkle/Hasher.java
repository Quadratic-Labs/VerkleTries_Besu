package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes32;

/**
 * Defines an interface for a Verkle Trie node hashing strategy.
 *
 * @param <V> The type of values to be hashed.
 */
public interface Hasher<V> {

    /**
     * Calculates the commitment hash for an array of inputs.
     *
     * @param inputs An array of values to be hashed.
     * @return The commitment hash calculated from the inputs.
     */
    public Bytes32 commit(V[] inputs);

    // public Bytes32 commit_sparse(V[] input, int[] index)
}