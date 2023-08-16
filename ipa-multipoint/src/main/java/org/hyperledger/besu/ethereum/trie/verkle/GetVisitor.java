package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;

public class GetVisitor<V> implements PathNodeVisitor<V> {

    @Override
    public Node<V> visit(BranchNode<V> branchNode, Bytes path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Node<V> visit(LeafNode<V> leafNode, Bytes path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Node<V> visit(NullNode<V> nullNode, Bytes path) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }
    
}
