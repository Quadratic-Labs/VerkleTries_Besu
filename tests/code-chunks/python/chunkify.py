# Code taken from EIP6800 (2023-11-15)
# https://notes.ethereum.org/@vbuterin/verkle_tree_eip#Code

import json
from typing import Sequence
bytes32 = bytes


PUSH_OFFSET = 95
PUSH1 = PUSH_OFFSET + 1
PUSH32 = PUSH_OFFSET + 32


def chunkify_code(code: bytes) -> Sequence[bytes32]:
    # Pad to multiple of 31 bytes
    if len(code) % 31 != 0:
        code += b'\x00' * (31 - (len(code) % 31))
    # Figure out how much pushdata there is after+including each byte
    bytes_to_exec_data = [0] * (len(code) + 32)
    pos = 0
    while pos < len(code):
        if PUSH1 <= code[pos] <= PUSH32:
            pushdata_bytes = code[pos] - PUSH_OFFSET
        else:
            pushdata_bytes = 0
        pos += 1
        for x in range(pushdata_bytes):
            bytes_to_exec_data[pos + x] = pushdata_bytes - x
        pos += pushdata_bytes
    # Output chunks
    return [
        bytes([min(bytes_to_exec_data[pos], 31)]) + code[pos: pos+31]
        for pos in range(0, len(code), 31)
    ]


def main():
    with open("erc20-bytecode.hex") as f:
        bytecode = f.read().strip()
    chunks = chunkify_code(bytes.fromhex(bytecode))
    return {
        "bytecode": bytecode,
        "chunks": [x.hex() for x in chunks],
    }


if __name__ == "__main__":
    print(json.dumps(main(), indent=2))
