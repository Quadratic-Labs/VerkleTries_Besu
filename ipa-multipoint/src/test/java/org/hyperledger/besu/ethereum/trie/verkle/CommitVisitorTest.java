package org.hyperledger.besu.ethereum.trie.verkle;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class CommitVisitorTest {
    
    @Test
    public void testEmptyTrie() {
        NodeUpdaterMock<Bytes> nodeUpdater = new NodeUpdaterMock<Bytes>();
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        trie.commit(nodeUpdater);
        assertThat(nodeUpdater.storage.isEmpty());
    }

    @Test
    public void testOneValue() {
        NodeUpdaterMock<Bytes> nodeUpdater = new NodeUpdaterMock<Bytes>();
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        trie.put(key, value);
        trie.commit(nodeUpdater);
        assertThat(nodeUpdater.storage.get(key)).isEqualTo((Bytes) value);
    }

    @Test
    public void testTwoValuesAtSameStem() throws Exception {
        NodeUpdaterMock<Bytes> nodeUpdater = new NodeUpdaterMock<Bytes>();
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        trie.put(key1, value1);
        trie.put(key2, value2);
        trie.commit(nodeUpdater);
        System.out.println(String.format("Keys: %s", nodeUpdater.storage.keySet()));
        assertThat(nodeUpdater.storage.get(key1)).isEqualTo((Bytes) value1);
        assertThat(nodeUpdater.storage.get(key2)).isEqualTo((Bytes) value2);
    }

}
