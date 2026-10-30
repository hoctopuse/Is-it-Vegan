package com.example.isitvegan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProbableDutchHeadingResearchTest {
    @Test fun repetitionsAreNotIndependentLexicalClues() {
        val repeated = "INGREDIENTE.\nSuiker, suiker, suiker.\nZUTATEN: Zucker"
        val distinct = "INGREDIENTE.\nSuiker, tarwebloem, fructosestroop.\nZUTATEN: Zucker"
        assertTrue(LabelLanguageSegmenter.probableDutchHeadings(repeated).isEmpty())
        assertEquals(1, LabelLanguageSegmenter.probableDutchHeadings(distinct).size)
        assertTrue(LabelLanguageSegmenter.segment(distinct).blocks.none { it.headingConfidence == HeadingConfidence.PROBABLE })
    }
}
