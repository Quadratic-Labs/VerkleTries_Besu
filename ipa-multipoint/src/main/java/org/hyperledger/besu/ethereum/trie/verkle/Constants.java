package org.hyperledger.besu.ethereum.trie.verkle;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public final class Constants {
	static final int NODE_WIDTH = 256;
    static final int STEM_SIZE = 31;
    static final int LEAF_VALUE_SIZE = 32;

    private Constants(){}

    static Bytes getStem(Bytes32 key) {
        return key.slice(0, Constants.STEM_SIZE);
    }

    static int getSuffix(Bytes32 key) {
        return key.slice(Constants.STEM_SIZE).toInt(LITTLE_ENDIAN);  // Assumes STEM_SIZE >= 28
    }

    static int getWordAtDepth(Bytes key, int depth) {
        return key.get(depth) & 0xff;  // cast signed byte to "unsigned" int
    }
}