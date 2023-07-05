package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.Bytes;

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
