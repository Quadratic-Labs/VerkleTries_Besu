package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

/**
 * A visitor for removing nodes in a Verkle Trie while preserving its structure.
 *
 * <p>This class implements the PathNodeVisitor interface and is used to visit and remove nodes in the Verkle Trie
 * while maintaining the Trie's structural integrity.
 *
 * @param <V> The type of values associated with the nodes.
 */
public class RemoveVisitor<V> implements PathNodeVisitor<V> {
    private final Node<V> NULL_NODE = NullNode.instance();


    /**
     * Merges a single branching node by updating and replacing its structure with the new node.
     *
     * @param node The node to be merged.
     * @param commonPath The common path between the node and the path.
     * @param pathSuffix The path suffix associated with the node.
     * @param nodeSuffix The remaining node suffix after the common path.
     * @return The merged node with the updated structure.
     */
    protected Node<V> mergeSingleBranching(
            final Node<V> node,
            final Bytes commonPath,
            final Bytes pathSuffix, 
            final Bytes nodeSuffix) {
        final Node<V> updatedNode = node.replacePath(nodeSuffix.slice(1));
        // Should also add byte to location
        BranchNode<V> newBranchNode = new BranchNode<V>(node.getLocation(), commonPath);
        newBranchNode.replaceChild(nodeSuffix.get(0), updatedNode);
        final Node<V> insertedNode = newBranchNode.child(pathSuffix.get(0)).accept(this, pathSuffix.slice(1));
        newBranchNode.replaceChild(pathSuffix.get(0), insertedNode);
        return newBranchNode;
    }

    /**
     * Visits a branch node to remove a node associated with the provided path and maintain the Trie's structure.
     *
     * @param branchNode The branch node to visit.
     * @param path The path associated with the node to be removed.
     * @return The updated branch node with the removed node and preserved structure.
     */
    @Override
    public Node<V> visit(BranchNode<V> branchNode, Bytes path) {
        final Bytes leafPath = branchNode.getPath();
        final Bytes commonPath = leafPath.commonPrefix(path);
        if (commonPath.compareTo(leafPath) != 0) {
            return branchNode;
        }
        final Bytes pathSuffix = path.slice(commonPath.size());
        final byte childIndex = pathSuffix.get(0);
        final Node<V> childNode = branchNode.child(childIndex).accept(this, pathSuffix.slice(1));
        branchNode.replaceChild(childIndex, childNode);
        Node<V> resultNode = maybeFlatten(branchNode);
        return resultNode;
    }

    /**
     * Visits a leaf node to remove a node associated with the provided path and maintain the Trie's structure.
     *
     * @param leafNode The leaf node to visit.
     * @param path The path associated with the node to be removed.
     * @return A null node, indicating the removal of the node.
     */
    @Override
    public Node<V> visit(LeafNode<V> leafNode, Bytes path) {
        final Bytes nodePath = leafNode.getPath();
        final Bytes commonPath = nodePath.commonPrefix(path);
        if (commonPath.compareTo(nodePath) != 0) {
            return leafNode;
        }
        return NULL_NODE;
    }

    /**
     * Visits a null node and returns a null node, indicating that no removal is required.
     *
     * @param nullNode The null node to visit.
     * @param path The path associated with the removal (no operation).
     * @return A null node, indicating no removal is needed.
     */
    @Override
    public Node<V> visit(NullNode<V> nullNode, Bytes path) {
        return NULL_NODE;
    }

    /**
     * Checks if the branch node should be flattened (merged with its only child) and performs the flattening operation.
     *
     * @param branchNode The branch node to consider for flattening.
     * @return The updated node after flattening or the original branch node if flattening is not applicable.
     */
    protected Node<V> maybeFlatten(BranchNode<V> branchNode) {
        final Optional<Byte> onlyChildIndex = findOnlyChild(branchNode.getChildren());
        // Many children => return node as is
        if (!onlyChildIndex.isPresent()) {
            return branchNode;
        }
        // One child => merge with child: replace the path of the only child and return it
        final Node<V> onlyChild = branchNode.child(onlyChildIndex.get());
        final Bytes completePath = Bytes.concatenate(branchNode.getPath(), Bytes.of(onlyChildIndex.get()), onlyChild.getPath());
        return onlyChild.replacePath(completePath);
    }

    /**
     * Finds the index of the only non-null child in the list of children nodes.
     *
     * @param children The list of children nodes.
     * @return The index of the only non-null child if it exists, or an empty optional if there is no or more than one non-null child.
     */
    private Optional<Byte> findOnlyChild(final List<Node<V>> children) {
        Optional<Byte> onlyChildIndex = Optional.empty();
        for (int i = 0; i < children.size(); ++i) {
            if (children.get(i) != NULL_NODE) {
                if (onlyChildIndex.isPresent()) {
                    return Optional.empty();
                }
                onlyChildIndex = Optional.of((byte) i);
            }
        }
        return onlyChildIndex;
    }
}
