package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes32;

public class UnknownNode implements VerkleNode {
    int depth;

    public UnknownNode() {
        this(0);
    }

    public UnknownNode(int depth) {
        this.depth = depth;
    }

    public void insert(Bytes32 key, Bytes32 value) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }
}
