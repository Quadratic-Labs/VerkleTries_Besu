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
        assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(Bytes32.ZERO);
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
        Bytes32 expectedRootHash = Bytes32.fromHexString("0x3da38afb3eb81c48e6b5ab4e362e34b5c9dd8cd7c4115a9117e2717d3cefd30e");
        assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
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
        Bytes32 expectedRootHash = Bytes32.fromHexString("0xc396a7848475f5ac733c916e79c3b70b240ddbe07d7b13cbdfb3d80238baa129");
        assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
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
        Bytes32 expectedRootHash = Bytes32.fromHexString("0x953e65176fdaf8682a182a3ab804a83e56b79d55506334565cfbfb5acf330643");
        assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
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
        Bytes32 expectedRootHash = Bytes32.fromHexString("0x93e0e732253644b0580af0a2d5d0d0bc82d1fb1a4fb9f0b49b65b5db9020b025");
        assertThat(trie.getRootHash()).as("Retrieve root hash").isEqualByComparingTo(expectedRootHash);
    }

    @Test
    public void testDeleteTwoValuesAtSameStem() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000001");
        Bytes32 key2 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000002");
        byte index1 = key1.get(31);
        byte index2 = key2.get(31);
        trie.put(key1, value1);
        trie.put(key2, value2);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        trie.remove(key1);
        assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a NullNode").isInstanceOf(NullNode.class);
        trie.remove(key2);
        assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index2)).as("Child at index of second key is a NullNode").isInstanceOf(NullNode.class);
    }

    @Test
    public void testDeleteTwoValuesAtDifferentIndex() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        byte index1 = key1.get(0);
        byte index2 = key2.get(0);
        trie.put(key1, value1);
        trie.put(key2, value2);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie's root is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        trie.remove(key1);
        assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a NullNode").isInstanceOf(NullNode.class);
        trie.remove(key2);
        assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index2)).as("Child at index of second key is a NullNode").isInstanceOf(NullNode.class);
    }

    @Test
    public void testDeleteTwoValuesWithDivergentStemsAtDepth2() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        byte index1 = key1.get(1);
        byte index2 = key2.get(1);
        trie.put(key1, value1);
        trie.put(key2, value2);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie's root is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        trie.remove(key1);
        assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a NullNode").isInstanceOf(NullNode.class);
        trie.remove(key2);
        assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index2)).as("Child at index of second key is a NullNode").isInstanceOf(NullNode.class);
    }

    @Test
    public void testDeleteThreeValues() throws Exception {
        SimpleVerkleTrie<Bytes32, Bytes32> trie = new SimpleVerkleTrie<Bytes32, Bytes32>();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
        Bytes32 value2 = Bytes32.fromHexString("0x0200000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key3 = Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddff");
        Bytes32 value3 = Bytes32.fromHexString("0x0300000000000000000000000000000000000000000000000000000000000000");
        byte index1 = key1.get(1);
        trie.put(key1, value1);
        trie.put(key2, value2);
        trie.put(key3, value3);
        Node<Bytes32> root = trie.getRoot();
        assertThat(root).as("Many values trie's root is a BranchNode").isInstanceOf(BranchNode.class);
        BranchNode<Bytes32> branchRoot = (BranchNode<Bytes32>) root;
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a LeafNode").isInstanceOf(LeafNode.class);
        trie.remove(key3);
        assertThat(trie.get(key3)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(trie.get(key2)).as("Retrieve second value").isEqualTo(Optional.of(value2));
        trie.remove(key2);
        assertThat(trie.get(key2)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(trie.get(key1)).as("Retrieve first value").isEqualTo(Optional.of(value1));
        trie.remove(key1);
        assertThat(trie.get(key1)).as("Make sure value is deleted").isEqualTo(Optional.empty());
        assertThat(branchRoot.child(index1)).as("Child at index of first key is a NullNode").isInstanceOf(NullNode.class);
    }
}
