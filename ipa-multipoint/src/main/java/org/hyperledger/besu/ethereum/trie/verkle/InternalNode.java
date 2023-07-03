package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

// Eventually add support for persisted Nodes?
//import org.hyperledger.besu.ethereum.trie.verkle.NodeUpdater;


public class InternalNode implements VerkleNode {
    VerkleNode[] children;
    int depth;
    Bytes path;
    Bytes commitment;

    public InternalNode() {
        this(0);
    }

    public InternalNode(int depth) {
        this.depth = depth;
        this.children = new VerkleNode[Constants.NODE_WIDTH];
        for (int i = 0; i < Constants.NODE_WIDTH; i++) {
            this.children[i] = new EmptyNode(depth + 1);
        }
    }

    public void insert(Bytes32 key, Bytes32 value) throws Exception {
        Bytes stem = Constants.getStem(key);
        int suffix = Constants.getSuffix(key);
        int index = key.get(depth) & 0xff;  // cast signed byte to "unsigned" int
        VerkleNode child = children[index];
        if (child instanceof UnknownNode) {
            throw new Exception("Missing node in stateless");
        } else if (child instanceof InternalNode) {
            InternalNode internalChild = (InternalNode) child;
            internalChild.insert(key, value);
            return;
        } else if (child instanceof EmptyNode) {
            LeafNode leafNode = new LeafNode(stem, depth + 1);
            leafNode.insert(suffix, value);
            children[index] = leafNode;
        } else if (child instanceof LeafNode) {
            LeafNode leafChild = (LeafNode) child;
            if (leafChild.stem.compareTo(stem) == 0) {
                leafChild.insert(suffix, value);
                return;
            }
            int nextWordInExistingKey = leafChild.stem.get(depth + 1) & 0xff;
            InternalNode newBranch = new InternalNode(depth + 1);
            children[index] = newBranch;
            newBranch.children[nextWordInExistingKey] = leafChild;
            leafChild.depth += 1;
            int nextWordInInsertedKey = stem.get(depth + 1) & 0xff;
            if (nextWordInInsertedKey == nextWordInExistingKey) {
                newBranch.insert(key, value);
                return;
            }
            LeafNode leaf = new LeafNode(stem, depth + 2);
            leaf.insert(suffix, value);
            newBranch.children[nextWordInInsertedKey] = leaf;
        } else {
            throw new Exception("Unknown node type");
        }
    }

    public void insertMany(Bytes stem, Bytes32[] values) {
        throw new UnsupportedOperationException("Unimplemented method 'insertMany'");
    }
}    