package com.petfoster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class PetDTO {

    @Data
    public static class CreatePetRequest {
        @NotBlank(message = "宠物名称不能为空")
        private String name;

        @NotBlank(message = "宠物种类不能为空")
        private String species;

        private String breed;
        private Integer age;
        private String dietNotes;
        private String medicalNotes;
        private String photoUrl;
    }

    @Data
    public static class UpdatePetRequest {
        private String name;
        private String species;
        private String breed;
        private Integer age;
        private String dietNotes;
        private String medicalNotes;
        private String photoUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetResponse {
        private Long id;
        private Long ownerId;
        private String ownerUsername;
        private String name;
        private String species;
        private String breed;
        private Integer age;
        private String dietNotes;
        private String medicalNotes;
        private String photoUrl;
        private String createdAt;
    }
}
