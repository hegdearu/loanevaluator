package com.rbih.loanevaluator.service.engine;

import com.rbih.loanevaluator.enums.RiskBand;
import org.springframework.stereotype.Component;

@Component
public class RiskClassifier {

    public RiskBand classify(int creditScore) {
        if (creditScore >= 750) {
            return RiskBand.LOW;
        } else if (creditScore >= 650) {
            return RiskBand.MEDIUM;
        } else if (creditScore >= 600) {
            return RiskBand.HIGH;
        }
        throw new IllegalArgumentException(
                "Credit score %d is below the minimum threshold of 600 for risk classification"
                        .formatted(creditScore));
    }
}