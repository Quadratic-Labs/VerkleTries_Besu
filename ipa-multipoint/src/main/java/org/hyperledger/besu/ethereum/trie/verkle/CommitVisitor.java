/*
 * Copyright Hyperledger Besu Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.hyperledger.besu.ethereum.trie.verkle;

import org.apache.tuweni.bytes.Bytes;

/**
 * A visitor class responsible for committing changes to nodes in a Trie tree.
 *
 * <p>It iterates through the nodes and stores the changes in the Trie structure.
 *
 * @param <V> The type of node values.
 */
public class CommitVisitor<V> implements PathNodeVisitor<V> {

    /**
     * The NodeUpdater used to store changes in the Trie structure.
     */
    protected final NodeUpdater nodeUpdater;

    /**
     * Constructs a CommitVisitor with a provided NodeUpdater.
     *
     * @param nodeUpdater The NodeUpdater used to store changes in the Trie structure.
     */
    public CommitVisitor(final NodeUpdater nodeUpdater) {
        this.nodeUpdater = nodeUpdater;
    }

    /**
     * Visits a BranchNode to commit any changes in the node and its children.
     *
     * @param branchNode The BranchNode being visited.
     * @param location The location in the Trie tree.
     * @return The visited BranchNode.
     */
    @Override
    public Node<V> visit(final BranchNode<V> branchNode, final Bytes location) {
        if (!branchNode.isDirty()) {
            return branchNode;
        }
        for (int i = 0; i < BranchNode.maxChild(); ++i) {
            Bytes index = Bytes.of(i);
            final Node<V> child = branchNode.child((byte) i);
            child.accept(this, Bytes.concatenate(location, index));
        }
        nodeUpdater.store(location, null, branchNode.getEncodedValue());
        return branchNode;
    }

    /**
     * Visits a LeafNode to commit any changes in the node.
     *
     * @param leafNode The LeafNode being visited.
     * @param location The location in the Trie tree.
     * @return The visited LeafNode.
     */
    @Override
    public Node<V> visit(final LeafNode<V> leafNode, final Bytes location) {
        if (!leafNode.isDirty()) {
            return leafNode;
        }
        nodeUpdater.store(location, null, leafNode.getEncodedValue());
        return leafNode;
    }

    /**
     * Visits a NullNode, indicating no changes to commit.
     *
     * @param nullNode The NullNode being visited.
     * @param location The location in the Trie tree.
     * @return The NullNode indicating no changes.
     */
    @Override
    public Node<V> visit(final NullNode<V> nullNode, final Bytes location) {
        return nullNode;
    }
}