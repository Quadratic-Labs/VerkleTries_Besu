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
use ark_ff::bytes::{FromBytes, ToBytes};
use bandersnatch::Fr;
use ipa_multipoint::lagrange_basis::LagrangeBasis;
use ipa_multipoint::multiproof::CRS;
use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jbyteArray, jobjectArray, jsize};

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
    let mut vec = Vec::with_capacity(len);
    for i in 0..length {
        let jbarray: jbyteArray = env.get_object_array_element(input, i).unwrap().cast();
        let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
        vec.push(Fr::read(barray.as_ref()).unwrap())
    }

    let poly = LagrangeBasis::new(vec);
    let crs = CRS::new(256, PEDERSEN_SEED);
    let result = crs.commit_lagrange_poly(&poly);
    let mut result_bytes = [0u8; 128];
    result.write(result_bytes.as_mut()).unwrap();
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

    // TODO: implement update a commitment by commiting
    // new = H(0, ..., new-old, 0, ...)  (non-zero at index)
    // commitment + new
    // Computing new should be optimised.
    let index_obj = env.get_object_array_element(input, i).expect("Failed to retrieve commitment value");
    let index: u16 = env.get_int_field(index_obj, "value", "I").expect("Failed to get int field").into();

    let jbarray: jbyteArray = env.get_object_array_element(input, 1).unwrap().cast();
    let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    let old = Fr::read(barray.as_ref()).unwrap());

    let jbarray: jbyteArray = env.get_object_array_element(input, 2).unwrap().cast();
    let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    let new = Fr::read(barray.as_ref()).unwrap());

    let jbarray: jbyteArray = env.get_object_array_element(input, 3).unwrap().cast();
    let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
    let old_commitment = Fr::read(barray.as_ref()).unwrap());

    // TODO: write update logic, where poly[index] updates from old to new
    let mut vec = Vec::with_capacity(256):
    let poly = LagrangeBasis::new(vec);
    let crs = CRS::new(256, PEDERSEN_SEED);
    let result = crs.commit_lagrange_poly(&poly);
    let mut result_bytes = [0u8; 128];
    result.write(result_bytes.as_mut()).unwrap();
    let javaarray = env.byte_array_from_slice(&result_bytes).expect("Couldn't convert to byte array");
    return javaarray;
}


#[cfg(test)]
mod tests {
    use std::ops::Deref;

    use ark_ff::biginteger::BigInteger256;
    use ark_ff::ToBytes;
    use bandersnatch::Fr;
    use hex;
    use jni::{InitArgsBuilder, JavaVM};

    use crate::Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit;

    #[test]
    fn commit_multiproof_lagrange() {
        let f1_from_repr = Fr::from(BigInteger256([
            0xc81265fb4130fe0c,
            0xb308836c14e22279,
            0x699e887f96bff372,
            0x84ecc7e76c11ad,
        ]));

        let mut f1_bytes = [0u8; 32];
        f1_from_repr.write(f1_bytes.as_mut()).unwrap();

        let jvm_args = InitArgsBuilder::default().build().unwrap();
        let jvm = JavaVM::new(jvm_args).unwrap();
        let guard = jvm.attach_current_thread().unwrap();
        let env = guard.deref();
        let class = env.find_class("java/lang/String").unwrap();
        let jarray = env.byte_array_from_slice(&f1_bytes).unwrap();
        let objarray = env.new_object_array(4, "java/lang/byte[]", jarray).unwrap();
        env.set_object_array_element(objarray, 1, jarray).expect("cannot set input");
        env.set_object_array_element(objarray, 2, jarray).expect("cannot set input");
        env.set_object_array_element(objarray, 3, jarray).expect("cannot set input");
        let result = Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_commit(*env, class, objarray);
        let result_u8 = env.convert_byte_array(result).unwrap();
        assert_eq!("0fc066481fb30a138938dc749fa3608fc840386671d3ee355d778ed4e1843117a73b5363f846b850a958dab228d6c181f6e2c1035dad9b3b47c4d4bbe4b8671adc36f4edb34ac17a093f1c183f00f6e4863a2b38a7470edd1739cc1fdbc6541bc3b7896389a3fe5f59cdefe3ac2f8ae89101c227395d6fc7bca05f138683e204", hex::encode(result_u8));
    }

    #[test]
    fn commit_multiproof_lagrange_known_input() {
        let mut vec = Vec::with_capacity(len);
        vec.insert(2, Fr::read(hex::decode("")).unwrap());
        for i in 0..length {
            let jbarray: jbyteArray = env.get_object_array_element(input, i).unwrap().cast();
            let barray = env.convert_byte_array(jbarray).expect("Couldn't read byte array input");
            vec.push(Fr::read(barray.as_ref()).unwrap())
        }

        let poly = LagrangeBasis::new(vec);
        let crs = CRS::new(256, PEDERSEN_SEED);
        let result = crs.commit_lagrange_poly(&poly);
        let mut result_bytes = [0u8; 128];
        result.write(result_bytes.as_mut()).unwrap();
        assert_eq!("0fc066481fb30a138938dc749fa3608fc840386671d3ee355d778ed4e1843117a73b5363f846b850a958dab228d6c181f6e2c1035dad9b3b47c4d4bbe4b8671adc36f4edb34ac17a093f1c183f00f6e4863a2b38a7470edd1739cc1fdbc6541bc3b7896389a3fe5f59cdefe3ac2f8ae89101c227395d6fc7bca05f138683e204", hex::encode(result_u8));
    }
}
