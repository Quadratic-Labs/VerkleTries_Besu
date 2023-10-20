package org.hyperledger.besu.nativelib.ipa_multipoint;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.nativelib.ipamultipoint.LibIpaMultipoint;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


public class PedersenCommitmentTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<TestData> JsonData() throws IOException {
        System.out.println(PedersenCommitmentTest.class.getResource("/").getPath());
        InputStream inputStream = PedersenCommitmentTest.class.getResourceAsStream("/pedersen_commitment_test.json");
        return objectMapper.readValue(inputStream, new TypeReference<List<TestData>>() {});
    }

    static class TestData {
        public ArrayList<String> frs;
        public String commitment;
    }

    @ParameterizedTest
    @MethodSource("JsonData")
    public void TestPolynomialCommitments(TestData testData) {
        List<Bytes> FrBytes = new ArrayList<>();
        for (int i = 0 ; i < 256; i++ ) {
            BigInteger decimalBigInt = new BigInteger(testData.frs.get(i));
            FrBytes.add(Bytes32.leftPad(Bytes.wrap(decimalBigInt.toByteArray())));
        }
        byte[] input = Bytes.concatenate(FrBytes).toArray();
        BigInteger result = Bytes32.wrap(LibIpaMultipoint.commit(input)).toBigInteger();
        BigInteger expected = new BigInteger(testData.commitment);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void TestGenerators() {
        List<Bytes> FrBytes = new ArrayList<>();
        for (int i = 0; i < 256; i++ ) {
            FrBytes.add(i, Bytes32.ZERO);
        }
        for (int i = 0; i < 256; i++ ) {
            FrBytes.set(i, Bytes32.leftPad(Bytes.of(1)));
            byte[] input = Bytes.concatenate(FrBytes).toArray();
            Bytes32 result = Bytes32.wrap(LibIpaMultipoint.commit(input));
            System.out.println(String.format("%s", result.toHexString()));
        }
    }
}
