package org.hyperledger.besu.ethereum.trie.verkle;

import static java.nio.ByteOrder.LITTLE_ENDIAN;
import org.apache.tuweni.bytes.Bytes;

public final class Constants {
	static final int NODE_WIDTH = 256;
    static final int STEM_SIZE = 31;
    static final int LEAF_VALUE_SIZE = 32;

    private Constants(){}

    static Bytes getStem(Bytes key) {
        return key.slice(0, Constants.STEM_SIZE);
    }

    static Bytes getSuffix(Bytes key) {
        return key.slice(Constants.STEM_SIZE);
    }
    static int getSuffixPosition(Bytes key) {
        // Assumes suffix is at most 4 bytes, in other words NODE_WIDTH < 2^32
        return getSuffix(key).toInt(LITTLE_ENDIAN);  // Assumes suffix size is <= 4 bytes
    }

    static int getWordAtDepth(Bytes key, int depth) {
        return key.get(depth) & 0xff;  // cast signed byte to "unsigned" int
    }
}