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

import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;


public class CommitVisitor<V> implements PathNodeVisitor<V> {

    protected final NodeUpdater nodeUpdater;
    protected final Function<V, Bytes> valueSerialiser;

    public CommitVisitor(final NodeUpdater nodeUpdater, final Function<V, Bytes> valueSerialiser) {
        this.nodeUpdater = nodeUpdater;
        this.valueSerialiser = valueSerialiser;
    }

    @Override
    public Node<V> visit(final BranchNode<V> branchNode, final Bytes location) {
        if (!branchNode.isDirty()) {
            return branchNode;
        }
        Bytes extendedLocation = Bytes.concatenate(location, branchNode.getPath());

        for (int i = 0; i < branchNode.maxChild(); ++i) {
            Bytes index = Bytes.of(i);
            final Node<V> child = branchNode.child((byte) i);
            child.accept(this, Bytes.concatenate(extendedLocation, index));
        }
        nodeUpdater.store(location, null, (Bytes) branchNode.getHash().get());
        return branchNode;
    }

    @Override
    public Node<V> visit(final LeafNode<V> leafNode, final Bytes location) {
        if (!leafNode.isDirty()) {
            return leafNode;
        }
        Bytes extendedLocation = Bytes.concatenate(location, leafNode.getPath());
        nodeUpdater.store(extendedLocation, null, valueSerialiser.apply(leafNode.getValue().get()));
        return leafNode;
    }

    @Override
    public Node<V> visit(final NullNode<V> nullNode, final Bytes location) {
        return nullNode;
    }
}