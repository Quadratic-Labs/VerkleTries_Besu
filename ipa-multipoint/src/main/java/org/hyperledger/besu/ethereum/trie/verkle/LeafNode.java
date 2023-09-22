package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class LeafNode<V> implements Node<V>{
    private final Optional<Bytes> location;
    protected final V value;
    private final Bytes path;
    private final Optional<Bytes32> hash;
    private boolean dirty = true;

    public LeafNode(
            final Optional<Bytes> location,
            final V value,
            final Bytes path,
            final Optional<Bytes32> hash) {
        this.location = location;
        this.value = value;
        this.path = path;
        this.hash = hash;
    }

    public LeafNode(
            final Optional<Bytes> location,
            final V value,
            final Bytes path) {
        this.location = location;
        this.path = path;
        this.value = value;
        hash = Optional.empty();
    }

    public LeafNode(final V value, final Bytes path) {
        location = Optional.empty();
        this.value = value;
        this.path = path;
        hash = Optional.empty();
    }

    @Override
    public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
        return visitor.visit(this, path);
    }

    @Override
    public Node<V> accept(final NodeVisitor<V> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Optional<V> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public Optional<Bytes> getLocation() {
        return location;
    }

    @Override
    public Bytes getPath() {
        return path;
    }

    @Override
    public List<Node<V>> getChildren() {
        throw new UnsupportedOperationException("LeafNode does not have children.");
    }

    @Override
    public Optional<Bytes32> getHash() {
        return hash;
    }

    public Node<V> replaceHash(Bytes32 hash) {
        return new LeafNode<V>(location, value, path, Optional.of(hash));
    }

    @Override
    public Node<V> replacePath(Bytes path) {
        return new LeafNode<V>(location, value, path, hash);
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public String print() {
        return "Leaf:"
            + getValue().map(Object::toString).orElse("empty");
    }
} 
