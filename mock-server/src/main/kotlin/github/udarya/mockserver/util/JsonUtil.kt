package github.udarya.mockserver.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

val mapper = ObjectMapper()

fun String.exclude(
    excludeFields: List<String>
): String {
    val tree: JsonNode = mapper.readTree(this)
    if (tree is ObjectNode) {
        tree.remove(excludeFields)
    }
    for (node in tree) {
        if (node is ObjectNode) {
            node.remove(excludeFields)
        }
    }
    return tree.toString()
}
