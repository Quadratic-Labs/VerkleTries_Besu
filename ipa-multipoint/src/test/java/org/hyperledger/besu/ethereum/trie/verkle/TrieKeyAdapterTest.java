package org.hyperledger.besu.ethereum.trie.verkle;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;

public class TrieKeyAdapterTest {
    Bytes32 address = Bytes32.fromHexString("0x000000000000000000000000112233445566778899aabbccddeeff00112233");
    TrieKeyAdapter adapter = new TrieKeyAdapter();
    
    @Test
    public void testStorageKey() {
        UInt256 storageKey = UInt256.valueOf(32);
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02860");
        assertThat(adapter.storageKey(address, storageKey)).isEqualTo(expected);
    }
    
    @Test
    public void testCodeChunkKey() {
        UInt256 chunkId = UInt256.valueOf(24);
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02898");
        assertThat(adapter.codeChunkKey(address, chunkId)).isEqualTo(expected);
    }
    
    @Test
    public void testVersionKey() {
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02800");
        assertThat(adapter.versionKey(address)).isEqualTo(expected);
    }
    
    @Test
    public void testBalanceKey() {
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02801");
        assertThat(adapter.balanceKey(address)).isEqualTo(expected);
    }
    
    @Test
    public void testNonceKey() {
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02802");
        assertThat(adapter.nonceKey(address)).isEqualTo(expected);
    }
    
    @Test
    public void testCodeKeccakKey() {
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02803");
        assertThat(adapter.codeKeccakKey(address)).isEqualTo(expected);
    }
    
    @Test
    public void testCodeSizeKey() {
        // Need to change this once commit is fixed
        Bytes32 expected = Bytes32.fromHexString("0x19582934c5f46e07f07644e137bcc59c81dd3c49feeb38d2ee224d8b91f02804");
        assertThat(adapter.codeSizeKey(address)).isEqualTo(expected);
    }
}
