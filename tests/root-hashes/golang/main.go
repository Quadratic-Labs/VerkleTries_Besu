package main

import (
	"fmt"
	"github.com/gballet/go-verkle"
)


func InsertOneValue() {
	var root = verkle.New()
	var hash verkle.Fr

	key_a := []byte{
		0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255,
		0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255,
	}
	value_a := []byte{
		16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	}
	if err := root.Insert(key_a, value_a, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	root.Commit()
	hash = *root.Hash()
	fmt.Printf("%x\n", hash.BytesLE())
}

func main() {
	InsertOneValue()
}
