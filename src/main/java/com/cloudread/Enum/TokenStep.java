package com.cloudread.Enum;

import lombok.Getter;

@Getter
public enum TokenStep {
    /**
     * Second
     */
    REGISTER_STEP(600, 10),

    PASSWORD_VERIFICATION(600, 5),

    EMAIL_VERIFICATION(600, 5),

    ALTERNATIVE_EMAIL_VERIFICATION(600, 5),

    ALTERNATIVE_EMAIL_CONFIRMED(600, 5),

    FINAL_STEP(120, 1),
    ;

    private final int timeToLive;

    private final int allowToUse;

    TokenStep(int timeToLive, int allowToUse) {
        this.timeToLive = timeToLive;
        this.allowToUse = allowToUse;
    }
}