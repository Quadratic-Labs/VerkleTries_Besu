package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.rlp.RLP;
import org.apache.tuweni.rlp.RLPWriter;

/**
 * Represents a leaf node in the Verkle Trie.
 *
 * @param <V> The type of the node's value.
 */
public class LeafNode<V> implements Node<V>{
    private final Optional<Bytes> location; // Location in the tree
    protected final V value; // Value associated with the node
    private final Bytes path; // Extension path
    private final Optional<Bytes32> hash; // Hash of the node
    private Optional<Bytes> encodedValue = Optional.empty(); // Encoded value
    private final Function<V, Bytes> valueSerializer; // Serializer function for the value
    private boolean dirty = true; // not persisted

    /**
     * Constructs a new LeafNode with  optional location, value, path, and optional hash.
     *
     * @param location The location of the node in the tree (Optional).
     * @param value The value associated with the node.
     * @param path The path or key of the node.
     * @param hash The hash of the node (Optional).
     */
    public LeafNode(
            final Optional<Bytes> location,
            final V value,
            final Bytes path,
            final Optional<Bytes32> hash) {
        this.location = location;
        this.value = value;
        this.path = path;
        this.hash = hash;
        this.valueSerializer = val -> (Bytes) val;
    }

    /**
     * Constructs a new LeafNode with a provided optional location, value, and path.
     *
     * @param location The location of the node in the tree (Optional).
     * @param value The value associated with the node.
     * @param path The path or key of the node.
     */
    public LeafNode(
            final Optional<Bytes> location,
            final V value,
            final Bytes path) {
        this.location = location;
        this.path = path;
        this.value = value;
        this.hash = Optional.empty();
        this.valueSerializer = val -> (Bytes) val;
    }

    /**
     * Constructs a new LeafNode with a value and path.
     *
     * @param value The value associated with the node.
     * @param path The path or key of the node.
     */
    public LeafNode(final V value, final Bytes path) {
        location = Optional.empty();
        this.value = value;
        this.path = path;
        hash = Optional.empty();
        this.valueSerializer = val -> (Bytes) val;
    }

    /**
     * Accepts a visitor for path-based operations on the node.
     *
     * @param visitor The path node visitor.
     * @param path The path associated with a node.
     * @return The result of the visitor's operation.
     */
    @Override
    public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
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
     * Get the value associated with the node.
     *
     * @return An optional containing the value of the node if available.
     */
    @Override
    public Optional<V> getValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Get the location of the node.
     *
     * @return An optional containing the location of the node if available.
     */
    @Override
    public Optional<Bytes> getLocation() {
        return location;
    }

    /**
     * Get the path or key of the node.
     *
     * @return The path of the node.
     */
    @Override
    public Bytes getPath() {
        return path;
    }

    /**
     * Get the children of the node. A leaf node does not have children, so this method
     * throws an UnsupportedOperationException.
     *
     * @return The list of children nodes (unsupported operation).
     * @throws UnsupportedOperationException if called on a leaf node.
     */
    @Override
    public List<Node<V>> getChildren() {
        throw new UnsupportedOperationException("LeafNode does not have children.");
    }

    /**
     * Get the hash of the node if available.
     *
     * @return An optional containing the hash of the node if available.
     */
    @Override
    public Optional<Bytes32> getHash() {
        return hash;
    }

    /**
     * Replace the hash of the node.
     *
     * @param hash The new hash to set.
     * @return A new node with the updated hash.
     */
    public Node<V> replaceHash(Bytes32 hash) {
        return new LeafNode<V>(location, value, path, Optional.of(hash));
    }

    /**
     * Replace the path of the node.
     *
     * @param path The new path to set.
     * @return A new node with the updated path.
     */
    @Override
    public Node<V> replacePath(Bytes path) {
        return new LeafNode<V>(location, value, path, hash);
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
        Bytes encodedVal = getValue().isPresent() ? valueSerializer.apply(getValue().get()) : Bytes.EMPTY;
        List<Bytes> values = Arrays.asList(Bytes.EMPTY, getPath(), encodedVal);
        Bytes result = RLP.encodeList(values, RLPWriter::writeValue);
        this.encodedValue = Optional.of(result);
        return result;
    }

    /**
     * Marks the node as needing to be persisted.
     */
    @Override
    public void markDirty() {
        dirty = true;
    }

    /**
     * Checks if the node needs to be persisted.
     *
     * @return True if the node needs to be persisted.
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Get a string representation of the node.
     *
     * @return A string representation of the node.
     */
    @Override
    public String print() {
        return "Leaf:"
            + getValue().map(Object::toString).orElse("empty");
    }
} 
