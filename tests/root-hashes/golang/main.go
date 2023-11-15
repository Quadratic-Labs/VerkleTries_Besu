package main

import (
	"fmt"
	"github.com/gballet/go-verkle"
)


func TestEmptyTrie() {
	var root = verkle.New()
	var hash verkle.Fr

	root.Commit()
	hash = *root.Hash()
	fmt.Printf("TestEmptyTrie -- Hash: %x\n", hash.BytesLE())
	// fmt.Println(verkle.ToDot(root))
}

func TestOneValue() {
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
	fmt.Printf("TestOneValue -- Hash: %x\n", hash.BytesLE())
	// fmt.Println(verkle.ToDot(root))
}

func TestTwoValuesAtSameStem() {
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
	key_b := []byte{
		0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255,
		0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 0,
	}
	value_b := []byte{
		1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	}
	if err := root.Insert(key_a, value_a, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	if err := root.Insert(key_b, value_b, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	root.Commit()
	hash = *root.Hash()
	fmt.Printf("TestTwoValuesAtSameStem -- Hash: %x\n", hash.BytesLE())
	// fmt.Println(verkle.ToDot(root))
}

func TestTwoValuesAtDifferentIndex() {
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
	key_b := []byte{
		255, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 255,
		0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238, 0,
	}
	value_b := []byte{
		1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	}
	if err := root.Insert(key_a, value_a, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	if err := root.Insert(key_b, value_b, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	root.Commit()
	hash = *root.Hash()
	fmt.Printf("TestTwoValuesAtDifferentIndex -- Hash: %x\n", hash.BytesLE())
}

func TestTwoValuesWithDivergentStemsAtDepth2() {
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
	key_b := []byte{
		0, 255, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238,
		255, 0, 17, 34, 51, 68, 85, 102, 119, 136, 153, 170, 187, 204, 221, 238,
	}
	value_b := []byte{
		1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	}
	if err := root.Insert(key_a, value_a, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	if err := root.Insert(key_b, value_b, nil); err != nil {
		fmt.Println("Bad insertion")
	}
	root.Commit()
	hash = *root.Hash()
	fmt.Printf("TestTwoValuesWithDivergentStemsAtDepth2() -- Hash: %x\n", hash.BytesLE())
}

func main() {
	TestEmptyTrie()
	TestOneValue()
	TestTwoValuesAtSameStem()
	TestTwoValuesAtDifferentIndex()
	TestTwoValuesWithDivergentStemsAtDepth2()
}
