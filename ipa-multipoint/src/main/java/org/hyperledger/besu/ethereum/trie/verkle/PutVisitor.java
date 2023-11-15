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
 * A visitor for inserting or updating values in a Verkle Trie.
 *
 * <p>This class implements the PathNodeVisitor interface and is used to visit and modify nodes in the Verkle Trie
 * while inserting or updating a value associated with a specific path.
 *
 * @param <V> The type of values to insert or update.
 */
public class PutVisitor<V> implements PathNodeVisitor<V> {
    private V value;

    /**
     * Constructs a new PutVisitor with the provided value to insert or update.
     *
     * @param value The value to be inserted or updated in the Verkle Trie.
     */
    public PutVisitor(V value) {
        this.value = value;
    }


    /**
     * Inserts or updates a value in a branch node.
     *
     * @param node The branch node to insert or update the value.
     * @param commonPath The common path between the node and the path.
     * @param pathSuffix The path suffix associated with the value.
     * @param nodeSuffix The remaining node suffix after the common path.
     * @return The updated node with the inserted or updated value.
     */
    protected Node<V> insertNewBranching(
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
     * Visits a branch node to insert or update a value associated with the provided path.
     *
     * @param branchNode The branch node to visit.
     * @param path The path associated with the value to insert or update.
     * @return The updated branch node with the inserted or updated value.
     */
    @Override
    public Node<V> visit(final BranchNode<V> branchNode, final Bytes path) {
        final Bytes nodePath = branchNode.getPath();
        final Bytes commonPath = nodePath.commonPrefix(path);
        final int commonPathLength = commonPath.size();
        final Bytes pathSuffix = path.slice(commonPathLength);
        final Bytes nodeSuffix = nodePath.slice(commonPathLength);
        if (commonPath.compareTo(nodePath) == 0) {
            final byte childIndex = pathSuffix.get(0);
            final Node<V> updatedChild = branchNode.child(childIndex).accept(this, pathSuffix.slice(1));
            branchNode.replaceChild(childIndex, updatedChild);
            if (updatedChild.isDirty()) {
                branchNode.markDirty();
            }
            return branchNode;
        } else {
            return insertNewBranching(branchNode, commonPath, pathSuffix, nodeSuffix);
        }
    }

    /**
     * Visits a leaf node to insert or update a value associated with the provided path.
     *
     * @param leafNode The leaf node to visit.
     * @param path The path associated with the value to insert or update.
     * @return The updated leaf node with the inserted or updated value.
     */
    @Override
    public Node<V> visit(final LeafNode<V> leafNode, final Bytes path) {
        /* Leaf node is used to store a value.
         * However, it is a mixture with ExtensionNode and can have a non-empty path:
         * An extension all the way to a LeafNode is stored as a single LeafNode
         */
        final Bytes nodePath = leafNode.getPath();
        final Bytes commonPath = nodePath.commonPrefix(path);
        final int commonPathLength = commonPath.size();
        final Bytes pathSuffix = path.slice(commonPathLength);
        final Bytes nodeSuffix = nodePath.slice(commonPathLength);
        if (commonPath.compareTo(nodePath) == 0) {
            final LeafNode<V> newNode = new LeafNode<V>(leafNode.getLocation(), value, path);
            newNode.markDirty();
            return newNode;
        }

        return insertNewBranching(leafNode, commonPath, pathSuffix, nodeSuffix);
    }

    /**
     * Visits a null node to insert or update a value associated with the provided path.
     *
     * @param nullNode The null node to visit.
     * @param path The path associated with the value to insert or update.
     * @return A new leaf node containing the inserted or updated value.
     */
    @Override
    public Node<V> visit(final NullNode<V> nullNode, final Bytes path) {
        return new LeafNode<V>(value, path);
    }
}