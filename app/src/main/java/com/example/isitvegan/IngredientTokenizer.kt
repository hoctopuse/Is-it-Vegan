package com.example.isitvegan

enum class NodeKind { INGREDIENT, COMPOSITE_INGREDIENT, SECTION_HEADING }

internal data class IngredientToken(
    val text: String,
    val depth: Int,
    val order: Int,
    val parentOrder: Int? = null,
    val kind: NodeKind = NodeKind.INGREDIENT
)

internal data class IngredientNode(
    val token: IngredientToken,
    val children: List<IngredientNode>
)

/** Splits a label and reconstructs section and sub-composition parentage. */
internal object IngredientTokenizer {
    private val sectionHeading = Regex(
        "(?i)^[\\p{L}][\\p{L}'’ -]{0,80}(?:\\(\\s*\\))?\\s*:$"
    )

    fun tokenize(text: String): List<IngredientToken> = flatten(tokenizeTree(text))

    fun tokenizeTree(text: String): List<IngredientNode> {
        data class MutableNode(var token: IngredientToken, val children: MutableList<MutableNode>)

        val roots = mutableListOf<MutableNode>()
        val nodesByOrder = mutableMapOf<Int, MutableNode>()
        val buffer = StringBuilder()
        val groupParents = mutableListOf<Int?>()
        var activeSection: Int? = null
        var pendingLineParent: MutableNode? = null
        var order = 0

        fun addNode(raw: String): MutableNode? {
            val value = raw.trim().trim('*', ' ').trimEnd('.').trim()
            if (TextNormalizer.normalize(value).isBlank()) return null
            val kind = if (sectionHeading.matches(value)) NodeKind.SECTION_HEADING else NodeKind.INGREDIENT
            val parentOrder = if (kind == NodeKind.SECTION_HEADING) null else groupParents.lastOrNull() ?: activeSection
            val depth = generateSequence(parentOrder) { nodesByOrder[it]?.token?.parentOrder }.count()
            val token = IngredientToken(
                text = if (kind == NodeKind.SECTION_HEADING) value.removeSuffix(":").trim() else value,
                depth = depth,
                order = order++,
                parentOrder = parentOrder,
                kind = kind
            )
            val node = MutableNode(token, mutableListOf())
            nodesByOrder[token.order] = node
            if (parentOrder == null) roots += node else nodesByOrder[parentOrder]?.children?.add(node)
            if (kind == NodeKind.SECTION_HEADING) activeSection = token.order
            return node
        }

        fun flush(): MutableNode? {
            val node = addNode(buffer.toString())
            buffer.clear()
            return node
        }

        text.forEachIndexed { index, character ->
            when {
                character == ':' && groupParents.isEmpty() -> {
                    buffer.append(character)
                    if (sectionHeading.matches(buffer.toString().trim())) flush()
                }
                character == '\n' -> pendingLineParent = flush() ?: pendingLineParent
                character in listOf(',', ';') -> {
                    flush()
                    pendingLineParent = null
                }
                character == '\r' -> Unit
                character == '.' && groupParents.isEmpty() &&
                    !(index > 0 && index + 1 < text.length && text[index - 1].isDigit() && text[index + 1].isDigit()) &&
                    (index == text.lastIndex || text.getOrNull(index + 1)?.isWhitespace() == true) -> flush()
                character == '(' || character == '[' -> {
                    val parent = flush() ?: pendingLineParent ?:
                        groupParents.lastOrNull()?.let(nodesByOrder::get)
                    pendingLineParent = null
                    if (parent != null && parent.token.kind == NodeKind.INGREDIENT) {
                        parent.token = parent.token.copy(kind = NodeKind.COMPOSITE_INGREDIENT)
                    }
                    groupParents += parent?.token?.order
                }
                character == ')' || character == ']' -> {
                    flush()
                    if (groupParents.isNotEmpty()) groupParents.removeAt(groupParents.lastIndex)
                }
                else -> {
                    buffer.append(character)
                    if (!character.isWhitespace()) pendingLineParent = null
                }
            }
        }
        flush()

        fun freeze(node: MutableNode): IngredientNode = IngredientNode(node.token, node.children.map(::freeze))
        return roots.map(::freeze)
    }

    private fun flatten(nodes: List<IngredientNode>): List<IngredientToken> = buildList {
        fun visit(node: IngredientNode) {
            add(node.token)
            node.children.forEach(::visit)
        }
        nodes.forEach(::visit)
    }
}
