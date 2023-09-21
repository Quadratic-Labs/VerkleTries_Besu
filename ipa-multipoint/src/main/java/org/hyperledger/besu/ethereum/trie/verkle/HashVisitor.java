package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.Arrays;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;


public class HashVisitor<V extends Bytes> implements NodeVisitor<V> {
    Hasher<Bytes32> hasher = new SHA256Hasher();    

    Node<V> hashValues(BranchNode<V> branchNode, Bytes location) {
        // Values are little endian
        // Values are decomposed into 16 lower bytes and 16 higher bytes
        // Lower bytes are appended with a 1 to signify that a value is present
        // Each part is hashed separately
        int size = branchNode.maxChild();
        Bytes32[] values = new Bytes32[size * 2];
        for (int i = 0; i < size; i++) {
            Optional<V> value = branchNode.child((byte) i).getValue();
            if (value.isPresent()) {
                // Values are little endian.
                // Low values are appended with 1
                values[2 * i] = Bytes32.rightPad(Bytes.concatenate(value.get().slice(0, 16), Bytes.of((byte) 1).reverse()));
                values[2 * i + 1] = Bytes32.rightPad(value.get().slice(16, 32));
            } else {
                values[2 * i] = Bytes32.ZERO;
                values[2 * i + 1] = Bytes32.ZERO;
            }
        }
        Bytes32[] extensionHashes = new Bytes32[4];
        extensionHashes[0] = Bytes32.rightPad(Bytes.of((byte) 1).reverse());  // extension marker
        extensionHashes[1] = Bytes32.rightPad(location);
        extensionHashes[2] = hasher.commit(Arrays.copyOfRange(values, 0, size));
        extensionHashes[3] = hasher.commit(Arrays.copyOfRange(values, size, 2 * size));
        Bytes32 hash = hasher.commit(extensionHashes);
        return branchNode.replaceHash(hash);
    }

    @Override
    public Node<V> visit(BranchNode<V> branchNode) {
        if (!branchNode.isDirty() && branchNode.getHash().isPresent()) {
            return branchNode;
        }
        Bytes location = branchNode.getLocation().get();
        if (location.size() == 31) {  // branch with leaf nodes as children
            return hashValues(branchNode, location);
        }
        // Regular internal node
        int size = branchNode.maxChild();
        Bytes32[] childCommits = new Bytes32[size];
        for (int i = 0; i < size; i++) {
            byte index = (byte) i;
            Node<V> child = branchNode.child(index).accept(this);
            childCommits[i] = child.getHash().get();
        }
        Bytes32 hash = hasher.commit(childCommits);
        return branchNode.replaceHash(hash);
    }

    @Override
    public Node<V> visit(LeafNode<V> leafNode) {
        return leafNode;
    }

    @Override
    public Node<V> visit(NullNode<V> nullNode) {
        return nullNode;
    }
}
