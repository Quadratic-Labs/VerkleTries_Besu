package org.hyperledger.besu.ethereum.trie.verkle;

// import org.apache.tuweni.bytes.Bytes;
// import org.hyperledger.besu.nativelib.ipamultipoint.LibIpaMultipoint;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.apache.tuweni.bytes.Bytes32;

public class InternalNodeTest {
    
    @Test
    public void testCreateRootHasDepth0() {
        InternalNode root = new InternalNode();
        assertThat(root.depth).isEqualTo(0);
        assertThat(root.children.length).isEqualTo(Constants.NODE_WIDTH);
    }

    @Test
    public void testCreateSingleNode() {
        InternalNode node = new InternalNode(3);
        assertThat(node.depth).isEqualTo(3);
    }

    @Test
    public void testInsertOne() throws Exception {
        InternalNode root = new InternalNode();
        Bytes32 key = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        root.insert(key, value);
        assertThat(root.children[0]).isInstanceOf(LeafNode.class);
        assertThat(root.children[1]).isInstanceOf(EmptyNode.class);
    }

    @Test
    public void testInsertTwoAtSameStem() throws Exception {
        InternalNode root = new InternalNode();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        root.insert(key1, value1);
        root.insert(key2, value2);
        assertThat(root.children[0]).isInstanceOf(LeafNode.class);
        assertThat(root.children[1]).isInstanceOf(EmptyNode.class);
    }

    @Test
    public void testInsertTwoAtDifferentIndex() throws Exception {
        InternalNode root = new InternalNode();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0xff112233445566778899aabbccddeeff00112233445566778899aabbccddee00");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        root.insert(key1, value1);
        root.insert(key2, value2);
        assertThat(root.children[0]).isInstanceOf(LeafNode.class);
        assertThat(root.children[1]).isInstanceOf(EmptyNode.class);
        assertThat(root.children[255]).isInstanceOf(LeafNode.class);
    }

    @Test
    public void testInsertTwoWithDivergentStemsAtDepth2() throws Exception {
        InternalNode root = new InternalNode();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00ff112233445566778899aabbccddeeff00112233445566778899aabbccddee");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        root.insert(key1, value1);
        root.insert(key2, value2);
        assertThat(root.children[0]).isInstanceOf(InternalNode.class);
        InternalNode node = (InternalNode) root.children[0];
        assertThat(node.children[17]).isInstanceOf(LeafNode.class);
        assertThat(node.children[255]).isInstanceOf(LeafNode.class);
    }

    @Test
    public void testDivergentAtLowDepth() throws Exception {
        InternalNode root = new InternalNode();
        Bytes32 key1 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff");
        Bytes32 value1 = Bytes32.fromHexString("0x1000000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key2 = Bytes32.fromHexString("0x00112233445566778899aabbccddeeff00112233445566778899aabbccdd00ff");
        Bytes32 value2 = Bytes32.fromHexString("0x0100000000000000000000000000000000000000000000000000000000000000");
        Bytes32 key3 = Bytes32.fromHexString("0x00112233445566778899aabbccddee0000112233445566778899aabbccddeeff");
        Bytes32 value3 = Bytes32.fromHexString("0x0010000000000000000000000000000000000000000000000000000000000000");
        root.insert(key1, value1);
        root.insert(key2, value2);
        root.insert(key3, value3);
        InternalNode cursor = root;
        for (int i = 0; i < 30; i++) {
            int index = Constants.getWordAtDepth(key1, i);
            assertThat(cursor.children[index]).isInstanceOf(InternalNode.class);
            cursor = (InternalNode) cursor.children[index];
        }
        assertThat(cursor.children[0]).isInstanceOf(LeafNode.class);
        assertThat(cursor.children[238]).isInstanceOf(LeafNode.class);
        cursor = root;
        for (int i = 0; i < 15; i++) {
            int index = Constants.getWordAtDepth(key3, i);
            assertThat(cursor.children[index]).isInstanceOf(InternalNode.class);
            cursor = (InternalNode) cursor.children[index];
        }
        assertThat(cursor.children[0]).isInstanceOf(LeafNode.class);
    }
}
