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
use std::convert::TryFrom;



use ark_ff::{Zero, PrimeField};
use banderwagon::{Fr, Element, multi_scalar_mul};

use ipa_multipoint::lagrange_basis::LagrangeBasis;
use ipa_multipoint::crs::CRS;
use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jbyteArray, jobjectArray, jsize};



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
    let constant = Fr::from(2u128 + 256u128*64u128);

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


// OLD CODE:

// Seed used to compute the 256 pedersen generators
// using try-and-increment
// Copied from rust-verkle: https://github.com/crate-crypto/rust-verkle/blob/581200474327f5d12629ac2e1691eff91f944cec/verkle-trie/src/constants.rs#L12
const PEDERSEN_SEED: &'static [u8] = b"eth_verkle_oct_2021";


#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit(env: JNIEnv,
                                                                                                 _class: JClass<'_>,
                                                                                                 input: jobjectArray)
                                                                                                 -> jbyteArray {
    let length = env.get_array_length(input).unwrap();
    let len = <usize as TryFrom<jsize>>::try_from(length)
        .expect("invalid jsize, in jsize => usize conversation");
    let vec = Vec::with_capacity(len);
    
    if len != 4 {
        env.throw_new("java/lang/IllegalArgumentException", "Invalid input length")
           .expect("Failed to throw exception");
        return std::ptr::null_mut(); // Return null pointer to indicate an error
    }    

    for i in 0..length {
        let jbarray: jbyteArray = env.get_object_array_element(input, i).unwrap().cast();
        let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
        let x : usize = 0;
        Element::from_bytes(&[barray[x]]);
        // x=x+1;
    }

    

    // let jbarray: jbyteArray = env.get_object_array_element(input, 1).unwrap().cast();
    // let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");

    // Element::from_bytes(&[barray[1]]);

    // let jbarray: jbyteArray = env.get_object_array_element(input, 2).unwrap().cast();
    // let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");

    // Element::from_bytes(&[barray[2]]);

    // let jbarray: jbyteArray = env.get_object_array_element(input, 3).unwrap().cast();
    // let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");

    // Element::from_bytes(&[barray[3]]);

    let poly = LagrangeBasis::new(vec);
    let crs = CRS::new(256, PEDERSEN_SEED);
    let result = crs.commit_lagrange_poly(&poly);
    let result_bytes = [0u8; 128];
    result.to_bytes();
    let javaarray = env.byte_array_from_slice(&result_bytes).expect("Couldn't convert to byte array");
    return javaarray;
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_update_commitment(env: JNIEnv,
                                                                                                 _class: JClass<'_>,
                                                                                                 input: jobjectArray)
                                                                                                 -> jbyteArray {
    // input = index, old, new, commitment
    let length = env.get_array_length(input).unwrap();
    let len = <usize as TryFrom<jsize>>::try_from(length)
        .expect("invalid jsize, in jsize => usize conversation");

    if len != 4 {
        env.throw_new("java/lang/IllegalArgumentException", "Invalid input length")
           .expect("Failed to throw exception");
        return std::ptr::null_mut(); // Return null pointer to indicate an error
    }    


    let index_obj = env.get_object_array_element(input, 0).expect("Failed to retrieve commitment value");
    let j_value = env.get_field(index_obj, "value", "I").expect("Failed to get field value");
    let _index = j_value.i().expect("Expected int value") as u16;

    let jbarray: jbyteArray = env.get_object_array_element(input, 1).unwrap().cast();
    let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    let _old  = Element::from_bytes(&[barray[1]]).unwrap();

    let jbarray: jbyteArray = env.get_object_array_element(input, 2).unwrap().cast();
    let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    let _new = Element::from_bytes(&[barray[2]]).unwrap();


    let jbarray: jbyteArray = env.get_object_array_element(input, 3).unwrap().cast();
    let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    let old_commitment = Element::from_bytes(&[barray[3]]).unwrap();

    
    let vec = vec![Fr::zero(); 256];
    let poly = LagrangeBasis::new(vec.clone());
    let crs = CRS::new(256, PEDERSEN_SEED);
    let new_commitment = crs.commit_lagrange_poly(&poly);
    let mut result = banderwagon::multi_scalar_mul(&[old_commitment],&[vec[0].clone()]);
    result = banderwagon::multi_scalar_mul(&[new_commitment],&[vec[0]]);
    

    let result_bytes = [0u8; 128];
    result.to_bytes();

    let javaarray = env.byte_array_from_slice(&result_bytes).expect("Couldn't convert to byte array");
    return javaarray;
}


#[cfg(test)]
mod tests {
    use std::ops::Deref;

    use ark_ff::{ToBytes, Zero};
    use bandersnatch::Fr;
    use jni::{InitArgsBuilder, JavaVM};
    use jni::objects::{JValue, JObject};

    use crate::Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit;
    use crate::Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_update_commitment;

    #[test]
    fn commit_and_update_commitment_multiproof_lagrange() {

        let jvm_args = InitArgsBuilder::default().build().unwrap();
        let jvm = JavaVM::new(jvm_args).unwrap();
        let guard = jvm.attach_current_thread().unwrap();
        let env = guard.deref();
        let class = env.find_class("java/lang/String").unwrap();
        let objclass = env.find_class("java/lang/Object").unwrap();


        // First let's test the commitment with some empty bytes

        let commit_jarray = env.byte_array_from_slice(&[0u8; 128]).unwrap();
        let commit_objarray = env.new_object_array(1, objclass, JObject::null()).unwrap();

        env.set_object_array_element(commit_objarray, 0, commit_jarray).expect("cannot set input");
        let commit_result = Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit(*env, class, commit_objarray);
        let empty_bytes_commit_result_u8 = env.convert_byte_array(commit_result).unwrap();

        // Now we update the commitment with another value
        let old_from_repr = Fr::zero();
        let new_from_repr = Fr::from(1);
        let mut old_bytes = [0u8; 32];
        let mut new_bytes = [0u8; 32];

        old_from_repr.write(old_bytes.as_mut()).unwrap();
        new_from_repr.write(new_bytes.as_mut()).unwrap();

        let index = 1;

        let old_jarray = env.byte_array_from_slice(&old_bytes).unwrap();
        let new_jarray = env.byte_array_from_slice(&new_bytes).unwrap();
        let commitment_jarray = env.byte_array_from_slice(&empty_bytes_commit_result_u8).unwrap();
        let objclass = env.find_class("java/lang/Object").unwrap();
        let objarray = env.new_object_array(4, objclass, JObject::null()).unwrap();
                
        let integer_class = env.find_class("java/lang/Integer").unwrap();
        let args = [JValue::from(index)];
        let java_integer = env.call_static_method(integer_class, "valueOf", "(I)Ljava/lang/Integer;", &args).unwrap().l().unwrap();
    
        env.set_object_array_element(objarray, 0, java_integer).expect("cannot set input");
        env.set_object_array_element(objarray, 1, old_jarray).expect("cannot set input");
        env.set_object_array_element(objarray, 2, new_jarray).expect("cannot set input");
        env.set_object_array_element(objarray, 3, commitment_jarray).expect("cannot set input");
        let result = Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_update_commitment(*env, class, objarray);
        let result_u8 = env.convert_byte_array(result).unwrap();

        // Compute the commitment of the array with already the value 1 at index 1, it should be the same as result_u8

        let mut nonzero_arr = [0u8; 128];
        nonzero_arr[0] = 1;

        let zero_arr = [0u8; 128];

        let non_zero_valid_commit_jarray = env.byte_array_from_slice(&nonzero_arr).unwrap();
        let valid_commit_jarray = env.byte_array_from_slice(&zero_arr).unwrap();
        let valid_commit_objarray = env.new_object_array(2, objclass, JObject::null()).unwrap();
        
        env.set_object_array_element(valid_commit_objarray, 0, valid_commit_jarray).expect("cannot set input");
        env.set_object_array_element(valid_commit_objarray, 1, non_zero_valid_commit_jarray).expect("cannot set input");
        let valid_commit_result = Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit(*env, class, valid_commit_objarray);
        let valid_commit_result_u8 = env.convert_byte_array(valid_commit_result).unwrap();

        // Check that the commitment has been updated
        assert_ne!(result_u8, empty_bytes_commit_result_u8);

        //Check that the commitment is the same as the valid one
        assert_eq!(result_u8, valid_commit_result_u8);

    }


    // #[test]
    // fn commit_multiproof_lagrange() {
    //     let f1_from_repr = Fr::from(BigInteger256([
    //         0xc81265fb4130fe0c,
    //         0xb308836c14e22279,
    //         0x699e887f96bff372,
    //         0x84ecc7e76c11ad,
    //     ]));

    //     let mut f1_bytes = [0u8; 32];
    //     f1_from_repr.write(f1_bytes.as_mut()).unwrap();

    //     let jvm_args = InitArgsBuilder::default().build().unwrap();
    //     let jvm = JavaVM::new(jvm_args).unwrap();
    //     let guard = jvm.attach_current_thread().unwrap();
    //     let env = guard.deref();
    //     let class = env.find_class("java/lang/String").unwrap();
    //     let jarray = env.byte_array_from_slice(&f1_bytes).unwrap();
    //     let objarray = env.new_object_array(4, "java/lang/byte[]", jarray).unwrap();
    //     env.set_object_array_element(objarray, 1, jarray).expect("cannot set input");
    //     env.set_object_array_element(objarray, 2, jarray).expect("cannot set input");
    //     env.set_object_array_element(objarray, 3, jarray).expect("cannot set input");
    //     let result = Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit(*env, class, objarray);
    //     let result_u8 = env.convert_byte_array(result).unwrap();
    //     assert_eq!("0fc066481fb30a138938dc749fa3608fc840386671d3ee355d778ed4e1843117a73b5363f846b850a958dab228d6c181f6e2c1035dad9b3b47c4d4bbe4b8671adc36f4edb34ac17a093f1c183f00f6e4863a2b38a7470edd1739cc1fdbc6541bc3b7896389a3fe5f59cdefe3ac2f8ae89101c227395d6fc7bca05f138683e204", hex::encode(result_u8));
    // }

    // #[test]
    // fn commit_multiproof_lagrange_known_input() {
    //     let mut vec = Vec::with_capacity(len);
    //     vec.insert(2, Fr::read(hex::decode("")).unwrap());
    //     for i in 0..length {
    //         let jbarray: jbyteArray = env.get_object_array_element(input, i).unwrap().cast();
    //         let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    //         vec.push(Fr::read(barray.as_ref()).unwrap())
    //     }

    //     let poly = LagrangeBasis::new(vec);
    //     let crs = CRS::new(256, PEDERSEN_SEED);
    //     let result = crs.commit_lagrange_poly(&poly);
    //     let mut result_bytes = [0u8; 128];
    //     result.write(result_bytes.as_mut()).unwrap();
    //     assert_eq!("0fc066481fb30a138938dc749fa3608fc840386671d3ee355d778ed4e1843117a73b5363f846b850a958dab228d6c181f6e2c1035dad9b3b47c4d4bbe4b8671adc36f4edb34ac17a093f1c183f00f6e4863a2b38a7470edd1739cc1fdbc6541bc3b7896389a3fe5f59cdefe3ac2f8ae89101c227395d6fc7bca05f138683e204", hex::encode(result_u8));
    // }
}