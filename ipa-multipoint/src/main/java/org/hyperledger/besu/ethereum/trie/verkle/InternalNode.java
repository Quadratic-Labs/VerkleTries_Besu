package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

// Eventually add support for persisted Nodes?
//import org.hyperledger.besu.ethereum.trie.verkle.NodeUpdater;
import org.hyperledger.besu.nativelib.ipamultipoint.LibIpaMultipoint;


public class InternalNode implements VerkleNode {
    VerkleNode[] children;
    int depth;
    Bytes path;
    Bytes commitment;
    boolean isDirty;

    public InternalNode() {
        this(0);
    }

    public InternalNode(int depth) {
        this.depth = depth;
        this.children = new VerkleNode[Constants.NODE_WIDTH];
        for (int i = 0; i < Constants.NODE_WIDTH; i++) {
            this.children[i] = new EmptyNode(depth + 1);
        }
        setCommitment(Bytes32.ZERO);
    }

    public Bytes getCommitment() {
        return commitment;
    }

    public void setCommitment(Bytes commitment) {
        this.commitment = commitment;
    }

    public void setCommitment() throws Exception {
        if ( ! isDirty ) {
            return;
        }
        byte[][] commitments = new byte[Constants.NODE_WIDTH][];
        for (int i = 0; i < Constants.NODE_WIDTH; i++) {
            VerkleNode child = children[i];
            if (child instanceof UnknownNode) {
                throw new Exception("Cannot commit with UnknownNode child");
            }
            child.setCommitment();
            commitments[i] = child.getCommitment().toArray();
        }
        setCommitment(Bytes.wrap(LibIpaMultipoint.commit(commitments)));
        isDirty = false;
    }

    public void insert(Bytes32 key, Bytes32 value) throws Exception {
        isDirty = true;
        Bytes stem = Constants.getStem(key);
        int suffix = Constants.getSuffix(key);
        int index = Constants.getWordAtDepth(key, depth);
        VerkleNode child = children[index];
        if (child instanceof UnknownNode) {
            throw new Exception("Missing node in stateless");
        } else if (child instanceof InternalNode) {
            InternalNode internalChild = (InternalNode) child;
            internalChild.insert(key, value);
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
            int nextWordInExistingKey = Constants.getWordAtDepth(leafChild.stem, depth + 1);
            InternalNode newBranch = new InternalNode(depth + 1);
            children[index] = newBranch;
            newBranch.children[nextWordInExistingKey] = leafChild;
            leafChild.depth += 1;
            int nextWordInInsertedKey = Constants.getWordAtDepth(stem, depth + 1);
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