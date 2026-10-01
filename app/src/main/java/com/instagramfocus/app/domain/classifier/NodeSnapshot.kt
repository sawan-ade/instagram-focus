package com.instagramfocus.app.domain.classifier

/**
 * An immutable, lightweight snapshot of an accessibility node.
 * Decouples the classifier from Android framework nodes, ensuring thread-safety,
 * deterministic unit testing, and protection against node recycling errors.
 */
data class NodeSnapshot(
    val text: String? = null,
    val contentDescription: String? = null,
    val viewIdResourceName: String? = null,
    val className: String? = null,
    val packageName: String? = null,
    val isScrollable: Boolean = false,
    val isClickable: Boolean = false,
    val childCount: Int = 0,
    val children: List<NodeSnapshot> = emptyList()
) {
    /**
     * Collects all text and content descriptions from this node and its descendants.
     */
    fun collectAllText(): List<String> {
        val list = mutableListOf<String>()
        fun traverse(node: NodeSnapshot) {
            node.text?.let { if (it.isNotBlank()) list.add(it.trim()) }
            node.contentDescription?.let { if (it.isNotBlank()) list.add(it.trim()) }
            for (child in node.children) {
                traverse(child)
            }
        }
        traverse(this)
        return list
    }

    /**
     * Collects all view resource IDs present in the subtree.
     */
    fun collectAllIds(): List<String> {
        val list = mutableListOf<String>()
        fun traverse(node: NodeSnapshot) {
            node.viewIdResourceName?.let { if (it.isNotBlank()) list.add(it) }
            for (child in node.children) {
                traverse(child)
            }
        }
        traverse(this)
        return list
    }

    /**
     * Finds if any node in the subtree matches a predicate.
     */
    fun any(predicate: (NodeSnapshot) -> Boolean): Boolean {
        if (predicate(this)) return true
        for (child in children) {
            if (child.any(predicate)) return true
        }
        return false
    }
}
