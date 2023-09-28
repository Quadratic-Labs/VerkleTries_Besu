package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;

public class RemoveVisitor<V> implements PathNodeVisitor<V> {
    private final Node<V> NULL_NODE_RESULT = NullNode.instance();
    @Override
    public Node<V> visit(BranchNode<V> branchNode, Bytes path) {
        final Bytes leafPath = branchNode.getPath();
        final Bytes commonPath = leafPath.commonPrefix(path);
        if (commonPath.compareTo(leafPath) != 0) {
            return branchNode;
        }
        final Bytes pathSuffix = path.slice(commonPath.size());
        final byte childIndex = pathSuffix.get(0);
        Node<V> childNode = branchNode.child(childIndex);
        if (childNode instanceof LeafNode) {
            // It's a LeafNode, cast it to LeafNode
            childNode = visit((LeafNode<V>) childNode, pathSuffix.slice(1));
            branchNode.replaceChild(childIndex, childNode);
        } else {
            // It's a BranchNode, cast it to BranchNode
            childNode = visit((BranchNode<V>) childNode, pathSuffix.slice(1));
            branchNode.replaceChild(childIndex, childNode);
        }
        return branchNode;
    }

    @Override
    public Node<V> visit(LeafNode<V> leafNode, Bytes path) {
        final Bytes nodePath = leafNode.getPath();
        final Bytes commonPath = nodePath.commonPrefix(path);
        if (commonPath.compareTo(nodePath) != 0) {
            return leafNode;
        }
        return NULL_NODE_RESULT;
    }

    @Override
    public Node<V> visit(NullNode<V> nullNode, Bytes path) {
        return NULL_NODE_RESULT;
    }
    
}
