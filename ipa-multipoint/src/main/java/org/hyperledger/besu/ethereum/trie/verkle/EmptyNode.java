package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes32;

public class EmptyNode implements VerkleNode{
    int depth;

    public EmptyNode() {
        this(0);
    }

    public EmptyNode(int depth) {
        this.depth = depth;
    }

    public void insert(Bytes32 key, Bytes32 value) throws Exception {
        throw new UnsupportedOperationException("Unimplemented method 'insert'");
    }
}
