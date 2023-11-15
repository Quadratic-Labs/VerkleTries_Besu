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
import org.apache.tuweni.bytes.Bytes32;

/**
 * An interface representing a node updater for storing nodes in the Verkle Trie.
 */
public interface NodeUpdater {

    /**
     * Store a node in the database with the specified location and hash.
     *
     * @param location The location of the node.
     * @param hash The hash of the node.
     * @param value The node represented as a list of bytes (e.g., RLP-encoded data).
     */
    void store(Bytes location, Bytes32 hash, Bytes value);
}
