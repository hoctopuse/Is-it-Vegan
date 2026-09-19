package com.example.isitvegan

import java.math.BigDecimal

enum class NodeKind { INGREDIENT, COMPOSITE_INGREDIENT, ADDITIVE, SECTION_HEADING }

internal data class IngredientToken(
    val text: String,
    val depth: Int,
    val order: Int,
    val parentOrder: Int? = null,
    val kind: NodeKind = NodeKind.INGREDIENT,
    val compositionAfterQuantity: Boolean = false,
    val quantityPercent: BigDecimal? = null,
    val functionalClass: String? = null,
    val isNano: Boolean = false,
    val variableProportions: Boolean = false,
    val hasAlternatives: Boolean = false,
    val sourceClaim: SourceClaim = SourceClaim.UNSPECIFIED,
    val sourceClaimText: String? = null,
    val childCount: Int = 0
)

/** Compatibility adapter; IngredientTreeParser is the only parsing implementation. */
internal object IngredientTokenizer {
    fun tokenize(text: String): List<IngredientToken> = flatten(IngredientTreeParser.parse(text))

    fun tokenizeTree(text: String): List<IngredientNode> = IngredientTreeParser.parse(text)

    fun flatten(nodes: List<IngredientNode>): List<IngredientToken> = buildList {
        var order = 0

        fun visit(node: IngredientNode, depth: Int, parentOrder: Int?) {
            val currentOrder = order++
            val kind = when {
                node.isSectionHeading -> NodeKind.SECTION_HEADING
                node.kind == IngredientNodeKind.COMPOSITE -> NodeKind.COMPOSITE_INGREDIENT
                node.kind == IngredientNodeKind.ADDITIVE -> NodeKind.ADDITIVE
                else -> NodeKind.INGREDIENT
            }
            add(
                IngredientToken(
                    text = node.rawText,
                    depth = depth,
                    order = currentOrder,
                    parentOrder = parentOrder,
                    kind = kind,
                    compositionAfterQuantity = node.kind == IngredientNodeKind.COMPOSITE &&
                        node.quantityPercent != null,
                    quantityPercent = node.quantityPercent,
                    functionalClass = node.functionalClass,
                    isNano = node.isNano,
                    variableProportions = node.variableProportions,
                    hasAlternatives = node.hasAlternatives,
                    sourceClaim = node.sourceClaim,
                    sourceClaimText = node.sourceClaimText,
                    childCount = node.children.size
                )
            )
            node.children.forEach { visit(it, depth + 1, currentOrder) }
        }

        nodes.forEach { visit(it, 0, null) }
    }
}
