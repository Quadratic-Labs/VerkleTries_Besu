use jni::JNIEnv;
use jni::objects::{JClass, JValue};
use jni::sys::jbyteArray;
use rust_verkle::{prover, verifier, crypto};

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_native_VerkleCrypto_groupToField(
    mut env: JNIEnv,
    _class: JClass,
    input: jbyteArray,
) -> jbyteArray {
    let input_data = env.get_array_elements(&input, jni::objects::ReleaseMode::Primitive).unwrap();
    let field_element = crypto::group_to_field(&input_data);
    
    env.byte_array_from_slice(&field_element).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_native_VerkleCrypto_batchGroupToField(
    mut env: JNIEnv,
    _class: JClass,
    inputs: jbyteArray,
) -> jbyteArray {
    let inputs_data = env.get_array_elements(&inputs, jni::objects::ReleaseMode::Primitive).unwrap();
    let results = crypto::batch_group_to_field(&inputs_data);
    
    env.byte_array_from_slice(&results).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_native_VerkleCrypto_prove(
    mut env: JNIEnv,
    _class: JClass,
    config_ptr: jlong,
    proof_input: jbyteArray,
) -> jbyteArray {
    // Use a pointer for the config struct as requested in the issue
    let config = unsafe { &*(config_ptr as *const crypto::Config) };
    let input_data = env.get_array_elements(&proof_input, jni::objects::ReleaseMode::Primitive).unwrap();
    
    let proof = prover::create_proof(config, &input_data).expect("Proof generation failed");
    
    env.byte_array_from_slice(&proof).unwrap()
}

#[no_mangle]
pub extern "system" fn Java_org_hyperledger_besu_native_VerkleCrypto_verify(
    mut env: JNIEnv,
    _class: JClass,
    config_ptr: jlong,
    proof_data: jbyteArray,
    verification_input: jbyteArray,
) -> jboolean {
    let config = unsafe { &*(config_ptr as *const crypto::Config) };
    let proof_bytes = env.get_array_elements(&proof_data, jni::objects::ReleaseMode::Primitive).unwrap();
    let input_bytes = env.get_array_elements(&verification_input, jni::objects::ReleaseMode::Primitive).unwrap();
    
    let is_valid = verifier::verify_proof(config, &proof_bytes, &input_bytes).unwrap_or(false);
    
    if is_valid { jni::sys::JNI_TRUE } else { jni::sys::JNI_FALSE }
}
