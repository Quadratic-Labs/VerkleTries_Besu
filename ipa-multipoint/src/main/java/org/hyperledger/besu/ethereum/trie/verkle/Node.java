/*
 * Copyright ConsenSys AG.
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * An interface representing a node in the Verkle Trie.
 *
 * @param <V> The type of the node's value.
 */
public interface Node<V> {

    Bytes32 EMPTY_HASH = Bytes32.ZERO;

    /**
     * Accept a visitor to perform operations on the node based on a provided path.
     *
     * @param visitor The visitor to accept.
     * @param path The path associated with a node.
     * @return The result of visitor's operation.
     */

    Node<V> accept(PathNodeVisitor<V> visitor, Bytes path);

    /**
     * Accept a visitor to perform operations on the node.
     *
     * @param visitor The visitor to accept.
     * @return The result of the visitor's operation.
     */
    Node<V> accept(NodeVisitor<V> visitor);

    /**
     * Get the path associated with the node.
     *
     * @return The path of the node.
     */
    default Bytes getPath() {
        return Bytes.EMPTY;
    };

    /**
     * Get the location of the node.
     *
     * @return An optional containing the location of the node if available.
     */
    default Optional<Bytes> getLocation() {
        return Optional.empty();
    }

    /**
     * Get the value associated with the node.
     *
     * @return An optional containing the value of the node if available.
     */
    default Optional<V> getValue() {
        return Optional.empty();
    };

    /**
     * Get the hash associated with the node.
     *
     * @return An optional containing the hash of the node if available.
     */
    default Optional<Bytes32> getHash() {
        return Optional.empty();
    };

    /**
     * Replace the path of the node.
     *
     * @param path The new path to set.
     * @return A new node with the updated path.
     */
    Node<V> replacePath(Bytes path);

    /**
     * Get the encoded value of the node.
     *
     * @return The encoded value of the node.
     */
    default Bytes getEncodedValue() {
        return Bytes.EMPTY;
    }

    /**
     * Get the children nodes of this node.
     *
     * @return A list of children nodes.
     */
    default List<Node<V>> getChildren() {
        return Collections.emptyList();
    }

    /** Marks the node as needs to be persisted */
    void markDirty();

    /**
     * Is this node not persisted and needs to be?
     *
     * @return True if the node needs to be persisted.
     */
    boolean isDirty();

    /**
     * Get a string representation of the node.
     *
     * @return A string representation of the node.
     */
    String print();
}