use ark_serialize::CanonicalSerialize;
use once_cell::sync::Lazy;
use verkle_trie::{
    database::memory_db::MemoryDb, Trie, TrieTrait,
    VerkleConfig,
};
use std::iter::zip;


pub static CONFIG: Lazy<VerkleConfig<MemoryDb>> = Lazy::new(|| {
    match VerkleConfig::new(MemoryDb::new()) {
        Ok(config) => config,
        Err(_) => {
            // An error means that the file was already created
            // Lets call open instead
            VerkleConfig::open(MemoryDb::new()).expect("should be infallible")
        }
    }
});

pub fn main() {
    let mut trie = Trie::new(CONFIG.clone());
    let keys: Vec<[u8; 32]> = vec!{
        [
            0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255,
            0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255,
        ]
    };
    let vals: Vec<[u8; 32]> = vec!{
        [
		    16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        ]
	};
    trie.insert(zip(keys, vals));
    // zip(key, val).for_each(|(k, v)| trie.insert((k, v))); 
    let root = trie.root_hash();
    let mut root_bytes = [0u8; 32];
    root.serialize(&mut root_bytes[..]).unwrap();
    println!("{}", hex::encode(root_bytes));
}
