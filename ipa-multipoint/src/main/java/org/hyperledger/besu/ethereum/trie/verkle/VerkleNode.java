package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes32;
// import org.apache.tuweni.bytes.Bytes;
// import org.hyperledger.besu.ethereum.trie.verkle.NodeUpdater;


public interface VerkleNode {
    // Insert or Update value into the tree
    void insert(Bytes32 key, Bytes32 value) throws Exception;

    // Delete a leaf with the given key
    // boolean delete(byte[] key, NodeUpdater updater) throws Exception;

    // Get value at a given key
    // byte[] get(byte[] key, NodeUpdater updater) throws Exception;

    // Commit computes the commitment of the node. The
    // result (the curve point) is cached.
    // Point commit();

    // Commitment is a getter for the cached commitment
    // to this node.
    // Point commitment();

    // Hash returns the field representation of the commitment.
    // Fr hash();

    // GetProofItems collects the various proof elements, and
    // returns them breadth-first. On top of that, it returns
    // one "extension status" per stem, and an alternate stem
    // if the key is missing but another stem has been found.
    // ProofElements getProofItems(KeyList keyList) throws Exception;

    // Serialize encodes the node to RLP.
    // byte[] serialize() throws Exception;

    // Copy a node and its children
    // VerkleNode copy();

    // toDot returns a string representing this subtree in DOT language
    // String toDot(String rootLabel, String edgeLabel);

}



