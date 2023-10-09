# VerkleTries_Besu

This library is maintained by OnlyDust contributors. Telegram group: https://t.me/+ua0UWPJIv0gwMTk0


In order to run this code please install java 17.0.7 and gradle 7.6.1.

Goal is to build verkle trie for Besu ethereum execution client: https://github.com/hyperledger/besu. The approach we are taking right now is separating trie logic with cryptography by writing trie logic with Java and calling cryptography layer via JNI built on top of https://github.com/crate-crypto/rust-verkle and https://github.com/crate-crypto/ipa_multipoint. Insipiration is taken from here: https://hackmd.io/@6iQDuIePQjyYBqDChYw_jg/H1xXvMatq

More info on verkle tries is here: https://verkle.info/



To compile ipa_multipoint JNI and run all tests run `./build.sh`.
