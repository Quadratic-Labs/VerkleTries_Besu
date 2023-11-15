package org.hyperledger.besu.ethereum.trie.verkle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.rlp.RLP;

/**
 * A factory for creating Verkle Trie nodes based on stored data.
 *
 * @param <V> The type of values stored in Verkle Trie nodes.
 */
public class StoredNodeFactory<V> implements NodeFactory<V>{
    private NodeLoader nodeLoader;
    private final Function<Bytes, V> valueDeserializer;

    /**
     * Creates a new StoredNodeFactory with the given node loader and value deserializer.
     *
     * @param nodeLoader The loader for retrieving stored nodes.
     * @param valueDeserializer The function to deserialize values from Bytes.
     */
    public StoredNodeFactory(NodeLoader nodeLoader, Function<Bytes, V> valueDeserializer) {
        this.nodeLoader = nodeLoader;
        this.valueDeserializer = valueDeserializer;
    }

    /**
     * Retrieves a Verkle Trie node from stored data based on the location and hash.
     *
     * @param location The location of the node.
     * @param hash The hash of the node.
     * @return An optional containing the retrieved node, or an empty optional if the node is not found.
     */
    public Optional<Node<V>> retrieve(final Bytes location, final Bytes32 hash) {
        Optional<Bytes> optionalEncodedValues = nodeLoader.getNode(location, hash);
        if (optionalEncodedValues.isEmpty()) {
            return Optional.empty();
        }
        Bytes encodedValues = optionalEncodedValues.get();
        List<Bytes> values = RLP.decodeToList(encodedValues, reader -> reader.readValue().copy());
        Bytes hashOrEmpty = values.get(0);
        if (hashOrEmpty.isEmpty() && values.size() == 1) {  // NullNode
            return Optional.of(NullNode.instance()); 
        } 
        Bytes path = (Bytes) values.get(1);
        if (hashOrEmpty.isEmpty() && values.size() > 1) {  // LeafNode
            V value = valueDeserializer.apply((Bytes) values.get(2));
            return Optional.of(createLeafNode(location, path, value));
        }
        if (!hashOrEmpty.isEmpty()) {  // BranchNode
            Bytes32 savedHash = (Bytes32) hashOrEmpty;
            return Optional.of(createBranchNode(location, savedHash, path));
        }
        return Optional.empty();  // should not be here.
    }

    /**
     * Creates a BranchNode using the provided location, hash, and path.
     *
     * @param location The location of the BranchNode.
     * @param hash The hash of the BranchNode.
     * @param path The path associated with the BranchNode.
     * @return A BranchNode instance.
     */
    protected BranchNode<V> createBranchNode(Bytes location, Bytes32 hash, Bytes path) {
        int nChild = BranchNode.maxChild();
        ArrayList<Node<V>> children = new ArrayList<Node<V>>(nChild);
        for (int i=0; i < nChild; i++) {
            Optional<Node<V>> child = retrieve(Bytes.concatenate(location, Bytes.of(i)), hash);
            children.add(child.orElse(NullNode.instance()));
        }
        return new BranchNode<V>(location, hash, path, children);
    }

    /**
     * Creates a LeafNode using the provided location, path, and value.
     *
     * @param location The location of the LeafNode.
     * @param path The path associated with the LeafNode.
     * @param value The value stored in the LeafNode.
     * @return A LeafNode instance.
     */
    protected LeafNode<V> createLeafNode(Bytes location, Bytes path, V value) {
        return new LeafNode<V>(Optional.of(location), value, path);
    }
    
}
