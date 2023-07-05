package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.ethereum.trie.verkle.bandersnatch.Point;

public class LeafNode implements VerkleNode{
    Bytes stem;
    int depth;
    Bytes commitment;
    Bytes[] subCommitments;  // C1, C2 in EIP
    Bytes32[] values;
    boolean isDirty;

    Point C1;
    Point C2;

    Point commitment;

    public LeafNode(Bytes stem, int depth) {
        this.stem = stem;
        this.depth = depth;
        values = new Bytes32[Constants.NODE_WIDTH];
        for (int i = 0; i < Constants.NODE_WIDTH; i++) {
            values[i] = null;
        }
        isDirty = false;
    }

    public Bytes getCommitment() {
        return commitment;
    }

    public void setCommitment(Bytes commitment) {
        this.commitment = commitment;
    }

    public void setCommitment() {
        // TODO: implement this method
        // H(1, stem, C1, C2, 0, ...)
        // C1 = commitment of values' first half
        // C2 = commitment of values' second half
        // Values take 2 slots, with lower bits are padded with a single 1 (add 2^128).
        this.commitment = Bytes32.ZERO;
    }

    public void insert(Bytes32 key, Bytes32 value) throws Exception {
        Bytes stem = Constants.getStem(key);
        if (stem.compareTo(stem) != 0) {
            throw new Exception("Cannot insert key in LeafNode with different stem");
        }
        int suffix = Constants.getSuffix(key);
        insert(suffix, value);
    }

    public void insert(int suffix, Bytes32 value) throws Exception {
        values[suffix] = value;
    }
}
