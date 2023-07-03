package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.HashMap;
import java.util.Map;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class LeafNode implements VerkleNode{
    Bytes stem;
    int depth;
    Map<Integer, Bytes> values;  // TODO: use extension + suffix structure instead.

    public LeafNode(Bytes stem, int depth) {
        this.stem = stem;
        this.depth = depth;
        this.values = new HashMap<>(Constants.NODE_WIDTH);
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
        values.put(suffix, value);
    }
}
