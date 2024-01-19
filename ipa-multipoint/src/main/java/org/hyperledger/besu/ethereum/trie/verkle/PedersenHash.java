import java.security.NoSuchAlgorithmException;

public class PedersenHash {

    public static byte[] pedersenHash(byte[] input) throws NoSuchAlgorithmException {
        assert input.length <= 255 * 16;

        // Interpret input as list of 128 bit (16 byte) integers
        byte[] extInput = new byte[255 * 16];
        System.arraycopy(input, 0, extInput, 0, input.length);

        long[] ints = new long[256];
        ints[0] = 2 + 256 * input.length;

        for (int i = 0; i < 255; i++) {
            byte[] chunk = new byte[16];
            System.arraycopy(extInput, 16 * i, chunk, 0, 16);
            ints[i + 1] = bytesToLong(chunk);
        }

        // return computeCommitmentRoot(ints);      // computeCommitmentRoot() Should be implemented as well
    }
  
    private static long bytesToLong(byte[] bytes) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (bytes[i] & 0xFF);
        }
        return result;
    }
}
