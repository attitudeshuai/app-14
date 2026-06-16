package com.petfoster.util;

import com.petfoster.dto.*;
import com.petfoster.entity.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EntityMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }

    private static String getUsername(User user) {
        return user != null ? user.getUsername() : null;
    }

    public static AuthDTO.UserInfo toUserInfo(User user) {
        return new AuthDTO.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatar(),
                formatDateTime(user.getCreatedAt())
        );
    }

    public static PetDTO.PetResponse toPetResponse(Pet pet, User owner) {
        return PetDTO.PetResponse.builder()
                .id(pet.getId())
                .ownerId(pet.getOwnerId())
                .ownerUsername(getUsername(owner))
                .name(pet.getName())
                .species(pet.getSpecies())
                .breed(pet.getBreed())
                .age(pet.getAge())
                .dietNotes(pet.getDietNotes())
                .medicalNotes(pet.getMedicalNotes())
                .photoUrl(pet.getPhotoUrl())
                .createdAt(formatDateTime(pet.getCreatedAt()))
                .build();
    }

    public static FosterRequestDTO.RequestResponse toFosterRequestResponse(
            FosterRequest request, Pet pet, User owner, User fosterer) {
        return FosterRequestDTO.RequestResponse.builder()
                .id(request.getId())
                .petId(request.getPetId())
                .petName(pet != null ? pet.getName() : null)
                .ownerId(request.getOwnerId())
                .ownerUsername(getUsername(owner))
                .fostererId(request.getFostererId())
                .fostererUsername(getUsername(fosterer))
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .dailyCareNotes(request.getDailyCareNotes())
                .status(request.getStatus())
                .createdAt(formatDateTime(request.getCreatedAt()))
                .build();
    }

    public static DailyLogDTO.LogResponse toDailyLogResponse(FosterDailyLog log, User fosterer) {
        return DailyLogDTO.LogResponse.builder()
                .id(log.getId())
                .requestId(log.getRequestId())
                .fostererId(log.getFostererId())
                .fostererUsername(getUsername(fosterer))
                .logDate(log.getLogDate())
                .food(log.getFood())
                .mood(log.getMood())
                .photos(log.getPhotos())
                .note(log.getNote())
                .build();
    }

    public static ReviewDTO.ReviewResponse toReviewResponse(
            FosterReview review, User reviewer, User reviewee) {
        return ReviewDTO.ReviewResponse.builder()
                .id(review.getId())
                .requestId(review.getRequestId())
                .reviewerId(review.getReviewerId())
                .reviewerUsername(getUsername(reviewer))
                .revieweeId(review.getRevieweeId())
                .revieweeUsername(getUsername(reviewee))
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(formatDateTime(review.getCreatedAt()))
                .build();
    }
}
