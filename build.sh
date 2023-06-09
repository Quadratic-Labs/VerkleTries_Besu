#!/usr/bin/env bash

#############################
######### Variables #########
#############################

# Edit this variable to change the build options for secp256k1
SECP256K1_BUILD_OPTS="--enable-module-recovery"

#############################
####### End Variables #######
#############################

# Initialize external vars - need this to get around unbound variable errors
SKIP_GRADLE="$SKIP_GRADLE"

# Exit script if you try to use an uninitialized variable.
set -o nounset

# Exit script if a statement returns a non-true return value.
set -o errexit

# Use the error status of the first failure, rather than that of the last item in a pipeline.
set -o pipefail

# Resolve the directory that contains this script. We have to jump through a few
# hoops for this because the usual one-liners for this don't work if the script
# is a symlink
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SCRIPTDIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"

# Determine core count for parallel make
if [[ "$OSTYPE" == "linux-gnu" ]];  then
  CORE_COUNT=$(nproc)
  OSARCH=${OSTYPE%%[0-9.]*}-`arch`
fi

if [[ "$OSTYPE" == "darwin"* ]];  then
  CORE_COUNT=$(sysctl -n hw.ncpu)
  if [[ "`machine`" == "arm"* ]]; then
    arch_name="aarch64"
    export CFLAGS="-arch arm64"
  else
    arch_name="x86-64"
    export CFLAGS="-arch x86_64"
  fi
  OSARCH="darwin-$arch_name"
fi

# add to path cargo
[ -f $HOME/.cargo/env ] && . $HOME/.cargo/env

# add to path brew
[ -f $HOME/.zprofile ] && . $HOME/.zprofile

build_ipa_multipoint() {
  cat <<EOF
  ##################################
  ###### build ipa_multipoint ######
  ##################################
EOF

  cd "$SCRIPTDIR/ipa-multipoint/ipa_multipoint_jni"

  # delete old build dir, if exists
  rm -rf "$SCRIPTDIR/ipa-multipoint/build" || true
  mkdir -p "$SCRIPTDIR/ipa-multipoint/build/${OSARCH}/lib"

  cargo clean

  if [[ "$OSARCH" == "darwin-x86-64" ]];  then
    cargo build --lib --release --target=x86_64-apple-darwin
    lipo -create \
      -output target/release/libipa_multipoint_jni.dylib \
      -arch x86_64 target/x86_64-apple-darwin/release/libipa_multipoint_jni.dylib
    lipo -info ./target/release/libipa_multipoint_jni.dylib
  elif [[ "$OSARCH" == "darwin-aarch64" ]]; then
    cargo build --lib --release --target=aarch64-apple-darwin
    lipo -create \
      -output target/release/libipa_multipoint_jni.dylib \
      -arch arm64 target/aarch64-apple-darwin/release/libipa_multipoint_jni.dylib
    lipo -info ./target/release/libipa_multipoint_jni.dylib
  else
    cargo build --lib --release
  fi

  mkdir -p "$SCRIPTDIR/ipa-multipoint/build/${OSARCH}/lib"
  cp target/release/libipa_multipoint_jni.* "$SCRIPTDIR/ipa-multipoint/build/${OSARCH}/lib"
}


build_jars(){
  ########################
  ###### build jars ######
  ########################

  if [[ "$SKIP_GRADLE" != "true" ]]; then
    cd $SCRIPTDIR
    ./gradlew build
  fi
}

build_ipa_multipoint


build_jars
exit