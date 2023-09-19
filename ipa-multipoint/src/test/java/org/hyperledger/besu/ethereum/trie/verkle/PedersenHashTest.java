package org.hyperledger.besu.ethereum.trie.verkle;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class PedersenHashTest {
    
    @Test
    public void testShortByte() {
        PedersenHash hasher = new PedersenHash();
        Bytes msg = Bytes.fromHexString("0xef342498");
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x60caa1655cd0e7b73a0de01e26ba12540a42f9f00f4597685b55a392267460e5");
        assertThat(hasher.digest(msg)).isEqualTo(expected);
    }
}
