package org.hyperledger.besu.ethereum.trie.verkle;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class LeafNode<V> implements Node<V>{
    private final Optional<Bytes> location;
    private final Bytes path;
    protected final V value;
    private Optional<Bytes32> hash = Optional.empty();
    private boolean dirty = true;

    public LeafNode(
            final Bytes location,
            final Bytes path,
            final V value) {
        this.location = Optional.ofNullable(location);
        this.path = path;
        this.value = value;
    }

    public LeafNode(
            final Bytes path,
            final V value) {
        this.location = Optional.empty();
        this.path = path;
        this.value = value;
    }

    @Override
    public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
        return visitor.visit(this, path);
    }

    @Override
    public Optional<V> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public Bytes getPath() {
        return path;
    }

    @Override
    public List<Node<V>> getChildren() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getChildren'");
    }

    @Override
    public Bytes getEncodedBytes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEncodedBytes'");
    }

    @Override
    public Bytes getEncodedBytesRef() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEncodedBytesRef'");
    }

    @Override
    public Bytes32 getHash() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHash'");
    }

    @Override
    public Node<V> replacePath(Bytes path) {
        LeafNode<V> updatedNode = new LeafNode<V>(location.get(), path, value);
        return updatedNode;
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'print'");
    }

    @Override
    public boolean isHealNeeded() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isHealNeeded'");
    }

    @Override
    public void markHealNeeded() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markHealNeeded'");
    }
}