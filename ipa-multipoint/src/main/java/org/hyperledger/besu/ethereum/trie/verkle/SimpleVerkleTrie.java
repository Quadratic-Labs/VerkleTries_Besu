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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;


public class SimpleVerkleTrie<K extends Bytes, V> implements VerkleTrie<K, V> {
    private Node<V> root;

    public SimpleVerkleTrie() {
        this.root = NullNode.instance();
    }

    @Override
    public Optional<V> get(final K key) {
        checkNotNull(key);
        return root.accept(getGetVisitor(), key).getValue();
    }

    @Override
    public void put(final K key, final V value) {
        checkNotNull(key);
        checkNotNull(value);
        this.root = root.accept(getPutVisitor(value), key);
    }
    @Override
    public void put(final K key, final PathNodeVisitor<V> putVisitor) {
        checkNotNull(key);
        this.root = root.accept(putVisitor, bytesToPath(key));
    }

    @Override
    public void remove(final K key) {
        checkNotNull(key);
        this.root = root.accept(getRemoveVisitor(), key);
    }

    @Override
    public Bytes32 getRootHash() {
        return root.getHash();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getRootHash() + "]";
    }

    @Override
    public void commit(final NodeUpdater nodeUpdater) {
        // Nothing to do here
    }
}