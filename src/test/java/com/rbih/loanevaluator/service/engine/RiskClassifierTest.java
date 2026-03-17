package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.enums.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskClassifierTest {

    private RiskClassifier riskClassifier;

    @BeforeEach
    void setUp() {
        riskClassifier = new RiskClassifier();
    }

    @ParameterizedTest
    @CsvSource({
            "750, LOW",
            "800, LOW",
            "900, LOW",
            "650, MEDIUM",
            "700, MEDIUM",
            "749, MEDIUM",
            "600, HIGH",
            "625, HIGH",
            "649, HIGH"
    })
    @DisplayName("Classify credit scores into correct risk bands")
    void shouldClassifyCorrectRiskBand(int creditScore, RiskBand expectedBand) {
        assertEquals(expectedBand, riskClassifier.classify(creditScore));
    }

    @Test
    @DisplayName("Should throw exception for credit score below 600")
    void shouldThrowForScoreBelowMinimum() {
        assertThrows(IllegalArgumentException.class,
                () -> riskClassifier.classify(599));
    }

    @Test
    @DisplayName("Boundary: 750 should be LOW, 749 should be MEDIUM")
    void shouldHandleBoundaryBetweenLowAndMedium() {
        assertEquals(RiskBand.LOW, riskClassifier.classify(750));
        assertEquals(RiskBand.MEDIUM, riskClassifier.classify(749));
    }

    @Test
    @DisplayName("Boundary: 650 should be MEDIUM, 649 should be HIGH")
    void shouldHandleBoundaryBetweenMediumAndHigh() {
        assertEquals(RiskBand.MEDIUM, riskClassifier.classify(650));
        assertEquals(RiskBand.HIGH, riskClassifier.classify(649));
    }
}
