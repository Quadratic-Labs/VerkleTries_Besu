package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * An interface representing a factory for creating nodes in the Verkle Trie.
 *
 * @param <V> The type of the nodes to be created.
 */
public interface NodeFactory<V> {

    /**
     * Retrieve a node with the given location and hash.
     *
     * @param location The location of the node.
     * @param hash The hash of the node.
     * @return An optional containing the retrieved node, or empty if not found.
     */
    Optional<Node<V>> retrieve(final Bytes location, final Bytes32 hash);
}