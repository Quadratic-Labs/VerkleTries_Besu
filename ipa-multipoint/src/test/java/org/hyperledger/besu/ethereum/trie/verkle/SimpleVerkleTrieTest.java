package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class SimpleVerkleTrieTest {
    
    @Test
    public void testEmptyTrie() {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Empty Trie is a NullNode").isInstanceOf(NullNode.class);
    }

    @Test
    public void testOneValue() {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        trie.put(key, value);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("One value trie's root is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(trie.get(key)).as("Get one value should be the inserted value").isEqualTo(Optional.of(value));
    }

    @Test
    public void testTwoValuesAtSameStem() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        Bytes path = Bytes.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee");
        byte index1 = key1.get(31);
        byte index2 = key2.get(31);
        byte index3 = (byte) 2;
        trie.put(key1, value1);
        trie.put(key2, value2);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(branchRoot.child(index2)).as("Child at index of second key is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(branchRoot.child(index3)).as("Child at another index is a NullNode").isInstanceOf(NullNode.class);
        assertThat(branchRoot.getPath()).as("Path is the common stem").isEqualTo(path);
        assertThat(trie.get(key1)).as("Retrieve first value").isEqualTo(Optional.of(value1));
        assertThat(trie.get(key2)).as("Retrieve second value").isEqualTo(Optional.of(value2));
    }

    @Test
    public void testTwoValuesAtDifferentIndex() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        Bytes path = Bytes.fromHexString("0x");
        byte index1 = key1.get(0);
        byte index2 = key2.get(0);
        byte index3 = (byte) 2;
        trie.put(key1, value1);
        trie.put(key2, value2);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie's root is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(branchRoot.child(index2)).as("Child at index of second key is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(branchRoot.child(index3)).as("Child at another index is a NullNode").isInstanceOf(NullNode.class);
        assertThat(branchRoot.getPath()).as("Path is the common stem").isEqualTo(path);
        assertThat(trie.get(key1)).as("Retrieve first value").isEqualTo(Optional.of(value1));
        assertThat(trie.get(key2)).as("Retrieve second value").isEqualTo(Optional.of(value2));
    }

    @Test
    public void testTwoValuesWithDivergentStemsAtDepth2() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        Bytes path = Bytes.fromHexString("0x00");
        byte index1 = key1.get(1);
        byte index2 = key2.get(1);
        byte index3 = (byte) 0;
        trie.put(key1, value1);
        trie.put(key2, value2);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie's root is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(branchRoot.child(index2)).as("Child at index of second key is a LeafNode").isInstanceOf(LeafNode.class);
        assertThat(branchRoot.child(index3)).as("Child at another index is a NullNode").isInstanceOf(NullNode.class);
        assertThat(branchRoot.getPath()).as("Path is the common stem").isEqualTo(path);
        assertThat(trie.get(key1)).as("Retrieve first value").isEqualTo(Optional.of(value1));
        assertThat(trie.get(key2)).as("Retrieve second value").isEqualTo(Optional.of(value2));
    }

}
