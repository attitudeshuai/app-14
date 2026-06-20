package com.petfoster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReputationDTO {

    private Integer totalScore;

    private String level;

    private Integer registerMonthsScore;

    private Integer completedFostersScore;

    private Integer averageRatingScore;

    private Integer defaultDeduction;

    private Long registerDays;

    private Long completedFosters;

    private Double averageRating;

    private Long reviewCount;

    private Integer defaultCount;

    public enum Level {
        EXCELLENT("优秀", 90, 100),
        GOOD("良好", 75, 89),
        FAIR("中等", 60, 74),
        POOR("较差", 40, 59),
        BAD("很差", 0, 39);

        private final String label;
        private final int min;
        private final int max;

        Level(String label, int min, int max) {
            this.label = label;
            this.min = min;
            this.max = max;
        }

        public String getLabel() {
            return label;
        }

        public static Level fromScore(int score) {
            for (Level level : values()) {
                if (score >= level.min && score <= level.max) {
                    return level;
                }
            }
            return BAD;
        }
    }
}
