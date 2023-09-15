package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;


public interface PathNodeVisitor<V> {

  Node<V> visit(BranchNode<V> branchNode, Bytes path);

  Node<V> visit(LeafNode<V> leafNode, Bytes path);

  Node<V> visit(NullNode<V> nullNode, Bytes path);
}