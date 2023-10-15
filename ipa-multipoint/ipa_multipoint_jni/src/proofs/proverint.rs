use crate::{
    constants::{CRS, PRECOMPUTED_WEIGHTS},
    errors::ProofCreationError,
    proof::opening_data::{OpeningData, Openings},
};
use crate::{
    errors::HintError,
};
use ark_serialize::{CanonicalDeserialize, CanonicalSerialize, Read, SerializationError, Write};

use banderwagon::Element;
use ipa_multipoint::multiproof::MultiPointProof;
use std::collections::{BTreeMap, BTreeSet};
use ipa_multipoint::{
    multiproof::{MultiPoint, ProverQuery},
    transcript::Transcript,
};

use verkle_trieproof::{VerkleProof, VerificationHint};
use itertools::Itertools;
use std::collections::BTreeSet;
use verkle_trie::database::{StemMeta, BranchMeta, Meta};
use verkle_trie::committer::{trie::Trie, TrieTrait};
use verkle_trie::committer::{Committer, PrecomputeLagrange, precompute::LagrangeTablePoints};
use verkle_spec::*;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::jbyteArray;

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_stemSerde(
    env: JNIEnv,
    _class:JClass,
    input: jbyteArray,
)->jbyteArray{

    let input = env.convert_byte_array(input).unwrap();
    let mut stem_bytes = Vec::<u8>::new();

    stem_bytes.resize(input.len(), 0);
    stem_bytes.copy_from_slice(&input[0..32])

    match StemMeta::from_bytes(stem_bytes) {
        Ok(stem_meta) => {
             println!("Deserialized stem successfully: {:?}", stem_meta);
        }

        Err(SerializationError::InvalidData) => {
            println!("Failed to deserialize stem: Invalid data!")
        }

        Err(_) => {
            println!("Failed for some unknown error!")
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_branchSerde(
    env: JNIEnv,
    _class:JClass,
    input: jbyteArray,
)->jbyteArray{

    let input = env.convert_byte_array(input).unwrap();
    let mut stem_bytes = Vec::<u8>::new();

    branch_bytes.resize(input.len(), 0);
    branch_bytes.copy_from_slice(&input[0..32])

    match BranchMeta::from_bytes(stem_bytes) {
        Ok(stem_meta) => {
             println!("Deserialized branch successfully: {:?}", stem_meta);
        }

        Err(SerializationError::InvalidData) => {
            println!("Failed to deserialize branch: Invalid data!")
        }

        Err(_) => {
            println!("Failed for some unknown error!")
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_nativelib_ipamultipoint_LibIpaMultipoint_updateRootBasic(
    env: JNIEnv,
    _class:JClass,
    input: jbyteArray,
)->jbyteArray{
    let input = env.convert_byte_array(input).unwrap();
    let mut root_bytes = Vec::<u8>::new();

    root_bytes.resize(input.len(), 0);
    root_bytes.copy_from_slice(&input[0..32])

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

    let mut trie = Trie::new(element);

    let mut keys = Vec::new();
    for i in 0..2 {
        let mut key_0 = [0u8, 32]
        key_0[0] = i;

        keys.push(key_0);
        Trie::insert_single(key_0, key_0);
    }
    let root = vec![]
    let meta = trie.storage.get_branch_meta(&root).unwrap();

    let proof = prover::create_verkle_proof(&trie.storage, keys.clone());

    let values: Vec<_> = keys.iter().map(|val| Some(*val).collect());

    let (ok, updated_hint) = proof.check(keys.clone(), values.clone(), meta.commitment());
    assert!(ok);


    let point = Element::prime_subgroup_generator();

    let lagrange_points = LagrangeTablePoints::precompute(&CRS.G)

    let new_root_commitment = update_root(
        updated_hint.unwrap(),
        keys.clone(),
        values,
        vec![Some([0u8, 32]), None],
        meta.commitment,
        lagrange_points
    )

    let mut got_bytes = [0u8; 32]
    group_to_field(&new_root_commitment.unwrap()).serialize(&mut got_bytes[..]).unwrap()

    new_root_commitment.reverse();
    return env.byte_array_from_slice(&new_root_commitment).expect("Couldn't convert to byte array");
}