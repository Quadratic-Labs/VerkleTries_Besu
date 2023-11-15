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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

/**
 * A simple implementation of a Verkle Trie.
 *
 * @param <K> The type of keys in the Verkle Trie.
 * @param <V> The type of values in the Verkle Trie.
 */
public class SimpleVerkleTrie<K extends Bytes, V extends Bytes> implements VerkleTrie<K, V> {
    private Node<V> root;

    /**
     * Creates a new Verkle Trie with a null node as the root.
     */
    public SimpleVerkleTrie() {
        this.root = NullNode.instance();
    }

    /**
     * Creates a new Verkle Trie with the specified node as the root.
     *
     * @param root The root node of the Verkle Trie.
     */
    public SimpleVerkleTrie(Node<V> root) {
        this.root = root;
    }

    /**
     * Retrieves the root node of the Verkle Trie.
     *
     * @return The root node of the Verkle Trie.
     */
    public Node<V> getRoot() {
        return root;
    }

    /**
     * Gets the value associated with the specified key from the Verkle Trie.
     *
     * @param key The key to retrieve the value for.
     * @return An optional containing the value if found, or an empty optional if not found.
     */
    @Override
    public Optional<V> get(final K key) {
        checkNotNull(key);
        return root.accept(new GetVisitor<V>(), key).getValue();
    }

    /**
     * Inserts a key-value pair into the Verkle Trie.
     *
     * @param key The key to insert.
     * @param value The value to associate with the key.
     */
    @Override
    public void put(final K key, final V value) {
        checkNotNull(key);
        checkNotNull(value);
        this.root = root.accept(new PutVisitor<V>(value), key);
    }

    /**
     * Removes a key-value pair from the Verkle Trie.
     *
     * @param key The key to remove.
     */
    @Override
    public void remove(final K key) {
        checkNotNull(key);
        this.root = root.accept(new RemoveVisitor<V>(), key);
    }

    /**
     * Computes and returns the root hash of the Verkle Trie.
     *
     * @return The root hash of the Verkle Trie.
     */
    @Override
    public Bytes32 getRootHash() {
        root = root.accept(new HashVisitor<V>(), root.getPath());
        return root.getHash().get();
    }

    /**
     * Returns a string representation of the Verkle Trie.
     *
     * @return A string in the format "SimpleVerkleTrie[RootHash]".
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getRootHash() + "]";
    }

    /**
     * Commits the Verkle Trie using the provided node updater.
     *
     * @param nodeUpdater The node updater for storing the changes in the Verkle Trie.
     */
    @Override
    public void commit(final NodeUpdater nodeUpdater) {
        root = root.accept(new HashVisitor<V>(), root.getPath());
        root = root.accept(new CommitVisitor<V>(nodeUpdater), Bytes.EMPTY);
    }
}