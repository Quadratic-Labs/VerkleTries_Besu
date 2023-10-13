#![allow(clippy::large_enum_variant)]
use verkle_trie::constants::{CRS, TWO_POLY_128};
use verkle_trie::database::{BranchMeta, StemMeta, Flush, Meta};
use verkle_trie::committer::{group_to_field,Trie, TrieTrait};
use ark_ff::{PrimeField, Zero};

use ark_serialize::{CanonicalDeserialize, CanonicalSerialize, Read, SerializationError, Write};

use banderwagon::{Element, Fr};
use std::ops::Mul;


//inserting into the tree when the key values are all zeroes
#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_insertKeyValue0(
    env: JNIEnv,
    _class:JClass,
    input1: jbyteArray,
    input2: jbyteArray,
)->jbyteArray{

    let input1 = env.convert_byte_array(input1).unwrap();
    let mut key_bytes = Vec::<u8>::new();

    key_bytes.resize(input1.len(), 0);
    key_bytes.copy_from_slice(&input1[0..32])

    match Element::from_bytes(root_bytes) {
        Ok(element) => {
            println!("Deserialized Element properly!", element)
        }
        Err(SerializationError::InvalidData) => {
            println!("Failed to deserialize Element: Invalid data");
        }
        Err(_) => {
            println!("Failed to deserialize Element: Unknown error");
        }
    }

    //TODO

}
