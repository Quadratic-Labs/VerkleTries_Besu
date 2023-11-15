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

/**
 * Defines a visitor interface for nodes in the Verkle Trie.
 *
 * @param <V> The type of value associated with nodes.
 */
public interface NodeVisitor<V> {

    /**
     * Visits a branch node.
     *
     * @param branchNode The branch node to visit.
     * @return The result of visiting the branch node.
     */
    Node<V> visit(BranchNode<V> branchNode);


    /**
     * Visits a leaf node.
     *
     * @param leafNode The leaf node to visit.
     * @return The result of visiting the leaf node.
     */
    Node<V> visit(LeafNode<V> leafNode);

    /**
     * Visits a null node.
     *
     * @param nullNode The null node to visit.
     * @return The result of visiting the null node.
     */
    Node<V> visit(NullNode<V> nullNode);
}