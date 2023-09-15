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

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public interface Node<V> {

    Bytes32 EMPTY_HASH = Bytes32.ZERO;

    Node<V> accept(PathNodeVisitor<V> visitor, Bytes path);
    // void accept(NodeVisitor<V> visitor);
    // void accept(Bytes location, LocationNodeVisitor<V> visitor);

    Bytes getPath();

    default Optional<Bytes> getLocation() {
        return Optional.empty();
    }

    Optional<V> getValue();

    List<Node<V>> getChildren();

    Bytes32 getHash();

    Node<V> replacePath(Bytes path);

    /** Marks the node as hash needs syncing */
    void markDirty();

    /**
     * Is this node not persisted and needs to be?
     *
     * @return True if the node needs to be persisted.
     */
    boolean isDirty();

    String print();
}