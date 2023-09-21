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



// Seed used to compute the 256 pedersen generators
// using try-and-increment
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