/*
 * Copyright Besu Contributors
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
 *
 */
package org.hyperledger.besu.nativelib.ipa_multipoint;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.nativelib.ipamultipoint.LibIpaMultipoint;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LibIpaMultipointTest {
  @Test
  public void testPedersenHash() {
    // I've put 2 in front for testing reason, real values will rarely start with 2.
    Bytes32 address = Bytes32.fromHexString("0x200000000000000000000000b794f5ea0ba39494ce839613fffba74279579268");
    Bytes32 trieIndex = Bytes32.fromHexString("0x2000000000000000000000000000000000000000000000000000000000000001");
    Bytes total = Bytes.wrap(address,trieIndex);
    String totalString = total.toHexString().substring(2); // subtract leading "0x"
    byte[] totalStringBytes = totalString.getBytes();
    byte[] result = LibIpaMultipoint.pedersenHash(totalStringBytes);

    // expected value
    Bytes total2 = Bytes32.fromHexString("0xbc27c15f46d538933a54f7cb793ab2310ee0f50996e3bac22c0acdc05715e95c");
    String totalString2 = total2.toHexString().substring(2); // subtract leading "0x"
    byte[] totalStringBytes2 = totalString2.getBytes();
    assertThat(result).isEqualTo(totalStringBytes2);
    System.out.println(Arrays.toString(result));
  }
}
