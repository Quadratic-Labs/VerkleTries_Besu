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

import java.util.ArrayList;

import org.apache.tuweni.bytes.Bytes;


public class PutVisitor<V> {
    private V value;

    @Override
    public Node<V> visit(final BranchNode<V> branchNode, final Bytes path) {
        assert path.size() > 0 : "Visiting path doesn't end with a non-matching terminator";
        final byte childIndex = path.get(0);
        final Node<V> updatedChild = branchNode.child(childIndex).accept(this, path.slice(1));
        Node<V> updatedNode = branchNode.replaceChild(childIndex, updatedChild);
        updatedNode.markDirty();
        return updatedNode;
    }

    @Override
    public Node<V> visit(final ExtensionNode<V> extensionNode, final Bytes path) {
        final Bytes extensionPath = extensionNode.getPath();
        final Bytes commonPath = extensionPath.commonPrefix(path);
        final int commonPathLength = commonPath.size();
        if (commonPath.compareTo(extensionPath) == 0) {
            List<Node<V>> updatedChildren = new ArrayList<>();
            for (Node<V> childNode: extensionNode.getChildren()) {
                updatedChildren.add(childNode.accept(this, path.slice(commonPathLength)));
            }
            Node<V> updatedNode = extensionNode.replaceChildren(updatedChildren);
            updatedNode.markDirty();
            return updatedNode;
        }
        // path diverges before the end of the extension - create a new branch
        final Bytes updatedPath = extensionPath.slice(commonPathLength + 1);
        final Bytes newPath = path.slice(commonPathLength + 1);

        final Node<V> updatedExtension = extensionNode.replacePath(extensionPath);
        final Node<V> newExtension = new ExtensionNode();  // nodeFactory.createExtension(newPath, value);
        final BranchNode<V> branch = new BranchNode();
        branch.replaceChild(newPath.get(0), newExtension);
        branch.replaceChild(extensionPath.get(0), updatedExtension);
        if (commonPathLength > 0) {
            branch.setPath(extensionPath.slice(0, commonPathLength));
        }
        newExtension.accept(this, newPath.slice(1));
        branch.markDirty();
        return branch;
    }

    @Override
    public Node<V> visit(final SuffixExtensionNode<V> suffixExtensionNode, final Bytes path) {
        return suffixExtensionNode;
    }

    @Override
    public Node<V> visit(final LeafNode<V> leafNode, final Bytes path) {
        final Bytes leafPath = leafNode.getPath();
        if 
    }

    @Override
    public Node<V> visit(final NullNode<V> nullNode, final Bytes path) {}
}