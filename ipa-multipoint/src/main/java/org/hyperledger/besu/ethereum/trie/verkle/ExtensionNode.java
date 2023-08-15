package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class ExtensionNode<V> implements Node<V> {
    private final Optional<Bytes> location;
    private final Bytes path;
    private Bytes32 hash;
    private final Node<V> child;

    public ExtensionNode(
            final Bytes location,
            final Bytes path,
            final Node<V> child) {
        assert (path.size() > 0);
        this.location = Optional.ofNullable(location);
        this.path = path;
        this.child = child;
    }

    public ExtensionNode(final Bytes path, final Node<V> child) {
        this.location = Optional.empty();
        this.path = path;
        this.child = child;
    }

    public Bytes getPath() {
        return path;
    }

    @Override
    public Node<V> accept(final PathNodeVisitor<V> visitor, final Bytes path) {
        return visitor.visit(this, path);
    }

    @Override
    public Optional<V> getValue() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getValue'");
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
