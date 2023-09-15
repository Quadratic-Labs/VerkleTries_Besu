package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;


public class BranchNode<V> implements Node<V> {
    @SuppressWarnings("rawtypes")
    protected static final Node NULL_NODE = NullNode.instance();

    private final Optional<Bytes> location;  // Location in the tree
    private final Bytes path;  // common children's trie-key prefix
    private Optional<Bytes32> hash = Optional.empty();  // vector commitment of children's commitments
    private boolean dirty = true;  // commitment out of sync
    private List<Node<V>> children;

    public BranchNode(
            final Bytes location,
            final Bytes path,
            final List<Node<V>> children) {
        assert (children.size() == maxChild());
        this.location = Optional.ofNullable(location);
        this.path = path;
        this.children = children;
    }

    public BranchNode(final Bytes location, final Bytes path) {
        this.location = Optional.ofNullable(location);
        this.path = path;
        this.children = new ArrayList<>();
        for (int i = 0; i < Constants.NODE_WIDTH; i++) {
            children.add(NULL_NODE);
        }
        hash = Optional.of(EMPTY_HASH);
    }

    public int maxChild() {
        return 256;
    }

    public Node<V> child(final byte childIndex) {
        return children.get(Byte.toUnsignedInt(childIndex));
    }

    public void replaceChild(final byte index, final Node<V> childNode) {
        children.set(Byte.toUnsignedInt(index), childNode);
    }

    public Bytes32 getHash() {
        return hash.get();
    }

    public void setHash(Bytes32 hash) {
        this.hash = Optional.of(hash);
    }

    public Bytes getPath() {
        return path;
    }

    @Override
    public Node<V> accept(PathNodeVisitor<V> visitor, Bytes path) {
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
    public Node<V> replacePath(Bytes path) {
        BranchNode<V> updatedNode = new BranchNode<V>(location.get(), path, children);
        return updatedNode;
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return !hash.isPresent() || dirty;
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