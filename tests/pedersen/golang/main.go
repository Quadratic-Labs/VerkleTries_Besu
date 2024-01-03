package main

import (
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"

	"github.com/crate-crypto/go-ipa/ipa"
	"github.com/crate-crypto/go-ipa/banderwagon"
)

// Define the structure of each object in the JSON list
type TestCase struct {
	Frs      []string `json:"frs"`
	Expected string   `json:"expected"`
}

func bytesEqual(a, b []byte) bool {
	if len(a) != len(b) {
		return false
	}
	for i := 0; i < len(a); i++ {
		if a[i] != b[i] {
			return false
		}
	}
	return true
}

func RunTestCase(cfg *ipa.IPAConfig, i int, obj TestCase) {
	var values []banderwagon.Fr
	var fr banderwagon.Fr
	for _, fr_str := range obj.Frs {
		fr_raw, _ := hex.DecodeString(fr_str)
		fr.SetBytes(fr_raw)
		values = append(values, fr)
	}
	commitment := cfg.Commit(values)
	actual := commitment.Bytes()
	expected, _ := hex.DecodeString(obj.Expected)
	result := bytesEqual(expected[:], actual[:])
	if result {
		fmt.Printf("Test[%d] OK\n", i)
	} else {
		fmt.Printf("Test[%d] ERR:\n\t actual: %x\n\t expected: %x\n", i, actual, expected)
	}
}


func main() {
	// Read testcases from json file
	file, err := os.Open("../genesis_lvl1_commits.json")
	if err != nil {
		fmt.Println("Error opening file:", err)
		return
	}
	defer file.Close()

	byteValue, _ := ioutil.ReadAll(file)
	var testCases []TestCase
	err = json.Unmarshal(byteValue, &testCases)
	if err != nil {
		fmt.Println("Error unmarshalling JSON:", err)
		return
	}

	// Run testcases
	cfg, _ := ipa.NewIPASettings()
	for i, testCase := range testCases {
		RunTestCase(cfg, i, testCase)
	}
}
