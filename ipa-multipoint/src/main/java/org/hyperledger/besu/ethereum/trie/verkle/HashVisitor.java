package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.Arrays;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * A visitor class for hashing operations on Verkle Trie nodes.
 *
 * @param <V> The type of the node's value.
 */
public class HashVisitor<V extends Bytes> implements PathNodeVisitor<V> {
    Hasher<Bytes32> hasher = new IPAHasher();

    /**
     * Visits a branch node, computes its hash, and returns a new branch node with the updated hash.
     *
     * @param branchNode The branch node to visit.
     * @param location The location associated with the branch node.
     * @return A new branch node with the updated hash.
     */
    @Override
    public Node<V> visit(BranchNode<V> branchNode, Bytes location) {
        if (!branchNode.isDirty() && branchNode.getHash().isPresent()) {
            return branchNode;
        }
        Bytes32 baseHash;
        if (location.size() == 31) {  // branch with leaf nodes as children
            baseHash = hashValues(branchNode, location);
        } else {  // Regular internal node
            int size = BranchNode.maxChild();
            Bytes32[] childCommits = new Bytes32[size];
            for (int i = 0; i < size; i++) {
                byte index = (byte) i;
                Node<V> child = branchNode.child(index);
                Bytes childPath = child.getPath();
                Bytes nextLocation = Bytes.concatenate(location, Bytes.of(index), childPath);
                Node<V> updatedChild = child.accept(this, nextLocation);
                branchNode.replaceChild(index, updatedChild);
                childCommits[i] = updatedChild.getHash().get();
            }
            baseHash = hasher.commit(childCommits);
        }
        return branchNode.replaceHash(hashExtension(branchNode.getPath(), baseHash));
    }

    /**
     * Visits a leaf node, computes its hash, and returns a new leaf node with the updated hash.
     *
     * @param leafNode The leaf node to visit.
     * @param location The location associated with the leaf node.
     * @return A new leaf node with the updated hash.
     */
    @Override
    public Node<V> visit(LeafNode<V> leafNode, Bytes location) {
        Bytes path = leafNode.getPath();
        if (path.size() == 0) {
            // LeafNode without extension should not be visited
            return leafNode;
        }
        // Remove last byte from location to get the stem
        Bytes stem = location.slice(0, location.size() - 1);
        Optional<V> value = leafNode.getValue();
        byte index = path.get(path.size() - 1);
        Bytes32 baseHash = hashStemExtensionOne(stem, value, index);
        return leafNode.replaceHash(hashExtension(path, baseHash));
    }

    /**
     * Visits a null node and returns the same null node.
     *
     * @param nullNode The null node to visit.
     * @param location The location associated with the null node.
     * @return The same null node.
     */
    @Override
    public Node<V> visit(NullNode<V> nullNode, Bytes location) {
        return nullNode;
    }

    /**
     * Computes the hash of a single value with a given index.
     * <p> Should use commit_one. For now, using commit.
     *
     * @param value The value to hash.
     * @param index The index associated with the value.
     * @return The hash of the value.
     */
    Bytes32 hashOne(Bytes32 value, byte index) {
        int idx = Byte.toUnsignedInt(index); 
        Bytes32[] values = new Bytes32[idx + 1];
        Arrays.fill(values, Bytes32.ZERO);
        values[idx] = value;
        return hasher.commit(values);
    }


    /**
     * Computes the hash of a branch node extension.
     *
     * @param path The path associated with the extension.
     * @param baseHash The base hash.
     * @return The hash of the branch node extension.
     */
    Bytes32 hashExtension(Bytes path, Bytes32 baseHash) {
        Bytes revPath = path.reverse();
        Bytes32 hash = baseHash;
        for (int i = 0; i < path.size(); i++) {
            hash = hashOne(hash, revPath.get(i));
        }
        return hash;
    }

    /**
     * Computes the hash of a branch node stem extension.
     *
     * @param stem The stem of the branch node.
     * @param leftValues The left values associated with the stem.
     * @param rightValues The right values associated with the stem.
     * @return The hash of the stem extension.
     */
    Bytes32 hashStemExtension(Bytes stem, Bytes32[] leftValues, Bytes32[] rightValues) {
        Bytes32[] extensionHashes = new Bytes32[4];
        extensionHashes[0] = Bytes32.rightPad(Bytes.of((byte) 1).reverse());  // extension marker
        extensionHashes[1] = Bytes32.rightPad(stem);
        extensionHashes[2] = hasher.commit(leftValues);
        extensionHashes[3] = hasher.commit(rightValues);
        return hasher.commit(extensionHashes);
    }


    /**
     * Computes the hash of a branch node stem extension with a single value.
     * <p> Should use commit_sparse to commit low and high values
     *
     * @param stem The stem of the branch node.
     * @param value The value associated with the stem extension.
     * @param index The index associated with the value.
     * @return The hash of the stem extension with a single value.
     */
    //
    Bytes32 hashStemExtensionOne(Bytes stem, Optional<V> value, byte index) {
        int idx = Byte.toUnsignedInt(index); 
        Bytes32 leftHash;
        Bytes32 rightHash;
        if (idx >= 128) {
            int rightIdx = idx - 128;
            Bytes32[] values = new Bytes32[rightIdx + 2];
            Arrays.fill(values, Bytes32.ZERO);
            values[rightIdx] = getLowValue(value);
            values[rightIdx + 1] = getHighValue(value);
            leftHash = Bytes32.ZERO;
            rightHash = hasher.commit(values);
        } else {
            Bytes32[] values = new Bytes32[idx + 2];
            Arrays.fill(values, Bytes32.ZERO);
            values[idx] = getLowValue(value);
            values[idx + 1] = getHighValue(value);
            leftHash = hasher.commit(values);
            rightHash = Bytes32.ZERO;
        }
        Bytes32[] extensionHashes = new Bytes32[4];
        extensionHashes[0] = Bytes32.rightPad(Bytes.of((byte) 1).reverse());  // extension marker
        extensionHashes[1] = Bytes32.rightPad(stem);
        extensionHashes[2] = leftHash;
        extensionHashes[3] = rightHash;
        return hasher.commit(extensionHashes);
    }

    /**
     * Retrieves the low value part of a given optional value.
     *
     * @param value The optional value.
     * @return The low value.
     */
    Bytes32 getLowValue(Optional<V> value) {
        // Low values have a flag at bit 128.
        if (!value.isPresent()) {
            return Bytes32.ZERO;
        }
        return Bytes32.rightPad(Bytes.concatenate(value.get().slice(0, 16), Bytes.of((byte) 1).reverse()));
    }

    /**
     * Retrieves the high value part of a given optional value.
     *
     * @param value The optional value.
     * @return The high value.
     */
    Bytes32 getHighValue(Optional<V> value) {
        if (!value.isPresent()) {
            return Bytes32.ZERO;
        }
        return Bytes32.rightPad(value.get().slice(16, 16));
    }

    /**
     * Computes the hash of values within a branch node.
     *
     * @param branchNode The branch node containing values.
     * @param location The location associated with the branch node.
     * @return The hash of the values within the branch node.
     */
    Bytes32 hashValues(BranchNode<V> branchNode, Bytes location) {
        // Values are little endian
        // Values are decomposed into 16 lower bytes and 16 higher bytes
        // Lower bytes are appended with a 1 to signify that a value is present
        // Each part is hashed separately
        int size = BranchNode.maxChild();
        Bytes32[] values = new Bytes32[size * 2];
        for (int i = 0; i < size; i++) {
            Optional<V> value = branchNode.child((byte) i).getValue();
            values[2 * i] = getLowValue(value);
            values[2 * i + 1] = getHighValue(value);
        }
        Bytes32[] leftValues = Arrays.copyOfRange(values, 0, size);
        Bytes32[] rightValues = Arrays.copyOfRange(values, size, 2 * size);
        return hashStemExtension(location, leftValues, rightValues);
    }
}
