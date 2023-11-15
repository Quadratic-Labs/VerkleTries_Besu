package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.rlp.RLP;
import org.apache.tuweni.rlp.RLPWriter;

/**
 * Represents a branch node in the Verkle Trie.
 *
 * @param <V> The type of the node's value.
 */
public class BranchNode<V> implements Node<V> {
    private final Optional<Bytes> location;  // Location in the tree
    private final Bytes path;  // Extension path
    private final Optional<Bytes32> hash;  // Vector commitment of children's commitments
    private Optional<Bytes> encodedValue = Optional.empty(); // Encoded value
    private List<Node<V>> children; // List of children nodes

    private boolean dirty = true;  // not persisted

    /**
     * Constructs a new BranchNode with location, hash, path, and children.
     *
     * @param location The location in the tree.
     * @param hash The vector commitment of children's commitments.
     * @param path The extension path.
     * @param children The list of children nodes.
     */
    public BranchNode(
            final Bytes location,
            final Bytes32 hash,
            final Bytes path,
            final List<Node<V>> children) {
        assert (children.size() == maxChild());
        this.location = Optional.of(location);
        this.hash = Optional.of(hash);
        this.path = path;
        this.children = children;
    }

    /**
     * Constructs a new BranchNode with optional location, optional hash, path, and children.
     *
     * @param location The optional location in the tree (Optional).
     * @param hash The optional vector commitment of children's commitments (Optional).
     * @param path The extension path.
     * @param children The list of children nodes.
     */
    public BranchNode(
            final Optional<Bytes> location,
            final Optional<Bytes32> hash,
            final Bytes path,
            final List<Node<V>> children) {
        assert (children.size() == maxChild());
        this.location = location;
        this.hash = hash;
        this.path = path;
        this.children = children;
    }

    /**
     * Constructs a new BranchNode with optional location, path, and children.
     *
     * @param location The optional location in the tree (Optional).
     * @param path The extension path.
     * @param children The list of children nodes.
     */
    public BranchNode(
            final Optional<Bytes> location,
            final Bytes path,
            final List<Node<V>> children) {
        assert (children.size() == maxChild());
        this.location = location;
        this.path = path;
        this.children = children;
        hash = Optional.empty();
    }

    /**
     * Constructs a new BranchNode with optional location and path, initializing children to NullNodes.
     *
     * @param location The optional location in the tree (Optional).
     * @param path The extension path.
     */
    public BranchNode(final Optional<Bytes> location, final Bytes path) {
        this.location = location;
        this.path = path;
        this.children = new ArrayList<>();
        for (int i = 0; i < maxChild(); i++) {
            children.add(NullNode.instance());
        }
        hash = Optional.of(EMPTY_HASH);
    }

    /**
     * Get the maximum number of children nodes (256 for byte indexes).
     *
     * @return The maximum number of children nodes.
     */
    public static int maxChild() {
        return 256;
    }

    /**
     * Accepts a visitor for path-based operations on the node.
     *
     * @param visitor The path node visitor.
     * @param path The path associated with a node.
     * @return The result of the visitor's operation.
     */
    @Override
    public Node<V> accept(PathNodeVisitor<V> visitor, Bytes path) {
        return visitor.visit(this, path);
    }

    /**
     * Accepts a visitor for generic node operations.
     *
     * @param visitor The node visitor.
     * @return The result of the visitor's operation.
     */
    @Override
    public Node<V> accept(final NodeVisitor<V> visitor) {
        return visitor.visit(this);
    }

    /**
     * Get the child node at a specified index.
     *
     * @param childIndex The index of the child node.
     * @return The child node.
     */
    public Node<V> child(final byte childIndex) {
        return children.get(Byte.toUnsignedInt(childIndex));
    }

    /**
     * Replaces the child node at a specified index with a new node.
     *
     * @param index The index of the child node to replace.
     * @param childNode The new child node.
     */
    public void replaceChild(final byte index, final Node<V> childNode) {
        children.set(Byte.toUnsignedInt(index), childNode);
    }

    /**
     * Get the vector commitment of children's commitments.
     *
     * @return An optional containing the vector commitment.
     */
    public Optional<Bytes32> getHash() {
        return hash;
    }

    /**
     * Replace the vector commitment with a new one.
     *
     * @param hash The new vector commitment to set.
     * @return A new BranchNode with the updated vector commitment.
     */
    public Node<V> replaceHash(Bytes32 hash) {
        return new BranchNode<V>(location, Optional.of(hash), path, children);
    }

    /**
     * Get the location in the tree.
     *
     * @return An optional containing the location if available.
     */
    @Override
    public Optional<Bytes> getLocation() {
        return location;
    }

    /**
     * Get the extension path of the node.
     *
     * @return The extension path.
     */
    public Bytes getPath() {
        return path;
    }

    /**
     * Replace the extension path with a new one.
     *
     * @param path The new extension path to set.
     * @return A new BranchNode with the updated extension path.
     */
    @Override
    public Node<V> replacePath(Bytes path) {
        BranchNode<V> updatedNode = new BranchNode<V>(location, path, children);
        return updatedNode;
    }

    /**
     * Get the RLP-encoded value of the node.
     *
     * @return The RLP-encoded value.
     */
    @Override
    public Bytes getEncodedValue() {
        if (encodedValue.isPresent()) {
            return encodedValue.get();
        }
        List<Bytes> values = Arrays.asList((Bytes) getHash().get(), getPath());
        Bytes result = RLP.encodeList(values, RLPWriter::writeValue);
        this.encodedValue = Optional.of(result);
        return result;
    }

    /**
     * Get the list of children nodes.
     *
     * @return The list of children nodes.
     */
    @Override
    public List<Node<V>> getChildren() {
        return children;
    }

    /** Marks the node as dirty, indicating that it needs to be persisted. */
    @Override
    public void markDirty() {
        dirty = true;
    }

    /**
     * Checks if the node is dirty, indicating that it needs to be persisted.
     *
     * @return `true` if the node is marked as dirty, `false` otherwise.
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }


    /**
     * Generates a string representation of the branch node and its children.
     *
     * @return A string representing the branch node and its children.
     */
    @Override
    public String print() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Branch:");
        for (int i = 0; i < maxChild(); i++) {
            final Node<V> child = child((byte) i);
            if (!Objects.equals(child, NullNode.instance())) {
                final String branchLabel = "[" + Integer.toHexString(i) + "] ";
                final String childRep = child.print().replaceAll("\n\t", "\n\t\t");
                builder.append("\n\t").append(branchLabel).append(childRep);
            }
        }
        return builder.toString();
    }
}
