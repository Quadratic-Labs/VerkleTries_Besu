package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;

/**
 * Visit the trie with a path parameter.
 * 
 * The path parameter indicates the path visited in the Trie.
 * As Nodes are (mostly) immutable, visiting a Node returns a (possibly) new Node that should replace the old one.
 * 
 */
public interface PathNodeVisitor<V> {

    /**
     * Visits a branch node with a specified path.
     *
     * @param branchNode The branch node to visit.
     * @param path The path associated with the visit.
     * @return The result of visiting the branch node.
     */
    Node<V> visit(BranchNode<V> branchNode, Bytes path);

    /**
     * Visits a leaf node with a specified path.
     *
     * @param leafNode The leaf node to visit.
     * @param path The path associated with the visit.
     * @return The result of visiting the leaf node.
     */
    Node<V> visit(LeafNode<V> leafNode, Bytes path);

    /**
     * Visits a null node with a specified path.
     *
     * @param nullNode The null node to visit.
     * @param path The path associated with the visit.
     * @return The result of visiting the null node.
     */
    Node<V> visit(NullNode<V> nullNode, Bytes path);
}