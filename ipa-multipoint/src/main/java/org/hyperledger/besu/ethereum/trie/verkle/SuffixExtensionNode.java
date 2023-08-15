package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class SuffixExtensionNode<V> implements Node<V>  {
    private final Optional<Bytes> location;
    private final Bytes path;  // path can store the stem.
    private Bytes32 hash;
    private Node<V> lowChild;
    private Node<V> highChild;
    private boolean dirty = false;

    public SuffixExtensionNode(
            final Bytes location,
            final Bytes path,
            final Node<V> lowChild,
            final Node<V> highChild) {
        this.location = Optional.ofNullable(location);
        this.path = path;
        this.lowChild = lowChild;
        this.highChild = highChild;
    }

    public SuffixExtensionNode(
            final Bytes path,
            final Node<V> lowChild,
            final Node<V> highChild) {
        this.location = Optional.empty();
        this.path = path;
        this.lowChild = lowChild;
        this.highChild = highChild;
    }

    @Override
    public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
        return visitor.visit(this, path);
    }

    @Override
    public Bytes getPath() {
        return path;
    }

    @Override
    public Optional<V> getValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getChildren'");
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'replacePath'");
    }

    @Override
    public void markDirty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'markDirty'");
    }

    @Override
    public boolean isDirty() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDirty'");
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
