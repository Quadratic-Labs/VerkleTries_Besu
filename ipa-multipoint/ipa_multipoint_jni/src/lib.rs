/*
 * Copyright Besu Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
use ark_ff::PrimeField;
use banderwagon::{Fr, multi_scalar_mul};
use ipa_multipoint::crs::CRS;
use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jbyteArray;

// Copied from rust-verkle: https://github.com/crate-crypto/rust-verkle/blob/581200474327f5d12629ac2e1691eff91f944cec/verkle-trie/src/constants.rs#L12
const PEDERSEN_SEED: &'static [u8] = b"eth_verkle_oct_2021";


#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit(env: JNIEnv,
                                                                                                 _class: JClass<'_>,
                                                                                                 input: jbyteArray)
                                                                                                 -> jbyteArray {
    // Input should be a multiple of 32-le-bytes.
    let inp = env.convert_byte_array(input).expect("Cannot convert jbyteArray to rust array");

    let len = inp.len();
    if len % 32 != 0 {
        env.throw_new("java/lang/IllegalArgumentException", "Invalid input length. Should be a multiple of 32-bytes.")
           .expect("Failed to throw exception");
        return std::ptr::null_mut(); // Return null pointer to indicate an error
    }    
    let n_scalars = len / 32;
    if n_scalars > 256 {
        env.throw_new("java/lang/IllegalArgumentException", "Invalid input length. Should be at most 256 elements of 32-bytes.")
           .expect("Failed to throw exception");
        return std::ptr::null_mut(); // Return null pointer to indicate an error
    }    

    // Each 32-le-bytes are interpreted as field elements.
    let mut scalars: Vec<Fr> = Vec::with_capacity(n_scalars);
    for b in inp.chunks(32) {
        scalars.push(Fr::from_le_bytes_mod_order(b));
    }

    // Committing all values at once.
    let bases = CRS::new(n_scalars, PEDERSEN_SEED);
    let mut commit = multi_scalar_mul(&bases.G, &scalars).to_bytes();
    commit.reverse();
    return env.byte_array_from_slice(&commit).expect("Couldn't convert to byte array")
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_pedersenHash(
    env: JNIEnv,
    _class: JClass,
    input: jbyteArray,
) -> jbyteArray {
    // First, we have to get the byte[] out of java.
    let ascii_bytes = env.convert_byte_array(input).unwrap();
    let mut helper_array: [u8; 128] = [0; 128];

    helper_array.copy_from_slice(&ascii_bytes);


    let mut merged_values: Vec<u8> = Vec::new();
    // Iterate over pairs of elements and merge them into u8 values so we can use them for the pedersen hash.
    for chunk in helper_array.chunks(2) {
        if chunk.len() == 2 {
            let merged_byte = (chunk[0] << 4) | (chunk[1] & 0x0F);
            merged_values.push(merged_byte);
        }
    }

    // Now we create the address and trie index variables.
    let mut address32 = [0u8; 32];
    address32.copy_from_slice(&merged_values[0..32]);
    let mut trie_index= [0u8; 32];
    trie_index.copy_from_slice(&merged_values[32..64]);

    // Now we compute the pedersen hash
    let mut result = pedersen_hash(&address32.to_vec(), &trie_index.to_vec());


    // TODO: make this prettier, this is very ugly.
    let mut build_string = String::from("");
    for i in 0..32 {
        let hex_string = format!("{:02x}", &result[i]);
        build_string = format!("{}{}", &build_string, hex_string.clone());
    }
    // TODO: remove next 3 lines, it's for testing purposes.
    // let mut str1 = "946352acd92aba2884d2b8746f44ae5fa1f61cc424af5c1a74c5c688862e2e48";
    // let mut str2 = re.as_str();
    // assert_eq!(str1, re.as_str());
    let result_array = turn_str_to_bytes(build_string.as_str());
    let output = env.byte_array_from_slice(&result_array).unwrap();
    output
}

/// Pedersen hash receives an address and a trie index and returns a hash calculated this way:
/// H(constant || address_low || address_high || trie_index_low || trie_index_high)
/// where constant = 2 + 256*64
/// address_low = first 16 bytes of the address
/// address_high = last 16 bytes of the address
/// trie_index_low = first 16 bytes of the trie index
/// trie_index_high = last 16 bytes of the trie index
/// The result is a 256 bit hash
/// TODO: make the parameters prettier
fn pedersen_hash(address_input : &Vec<u8>, trie_index_input : &Vec<u8>) -> [u8; 32] {
    let constant = Fr::from(2u128 + 256u128*64u128); // Hardcoded constant

    let address_reference_to_vec: &Vec<u8> = &address_input;
    let address: &[u8] = &*address_reference_to_vec;

    let trie_index_reference_to_vec: &Vec<u8> = &trie_index_input;
    let trie_index: &[u8] = &*trie_index_reference_to_vec;

    // TODO: remove next 2 lines, it's used for testing.
    // let address = "200000000000000000000000b794f5ea0ba39494ce839613fffba74279579268".as_bytes();
    // let trie_index = "2000000000000000000000000000000000000000000000000000000000000001".as_bytes();

    let address_low = Fr::from_le_bytes_mod_order(&address[0..16]);

    let address_high = Fr::from_le_bytes_mod_order(&address[16..32]);

    let trie_index_low = Fr::from_le_bytes_mod_order(&trie_index[0..16]);

    let trie_index_high = Fr::from_le_bytes_mod_order(&trie_index[16..32]);

    // Create a vector with the scalars. The first one is the constant, the rest are the values of the address and trie index
    let scalars = vec![constant, address_low.clone(), address_high.clone(), trie_index_low.clone(), trie_index_high.clone()];

    // Generate the CRS
    let bases = CRS::new(5, "eth_verkle_oct_2021".as_bytes());

    // Compute the multi scalar multiplication. The result is a point in the banderwagon group.
    let mut result = multi_scalar_mul(&bases.G, &scalars).to_bytes();

    // Reverse the result to get the correct order of the bytes.
    result.reverse();
    return result;
}

/// Helper function for Java JNI.
/// It receives a hexadecimal string and returns a [u8; 64] array
/// where each byte is the hexadecimal value of the letter in the string.
/// This is needed because of the Rust and Java interoperability.
fn turn_str_to_bytes(hex_string: &str) -> [u8; 64] {
    // Create a vector to store the u8 integers
    let mut u8_integers: Vec<u8> = Vec::new();

    // Iterate over each character in the hexadecimal string
    for hex_char in hex_string.chars() {
        let hex_val = hex_char as u8;
        u8_integers.push(hex_val);
    }

    let mut u8_array = [0u8; 64];

    // Convert the vector into an array of u8 integers
    u8_array.copy_from_slice(&u8_integers);

    u8_array
}
