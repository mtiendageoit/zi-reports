package com.zonainmueble.reports.validations;

import com.zonainmueble.reports.dto.ReportRequestDto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CoordinateValidator implements ConstraintValidator<ValidCoordinates, ReportRequestDto> {

    @Override
    public void initialize(ValidCoordinates constraintAnnotation) {
    }

    @Override
    public boolean isValid(ReportRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true; // Considerar que si el DTO es null, la validación no aplica
        }

        boolean validLatitude = dto.getLatitude() >= -90 && dto.getLatitude() <= 90;
        boolean validLongitude = dto.getLongitude() >= -180 && dto.getLongitude() <= 180;

        if (!validLatitude) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Latitude must be between -90 and 90.")
                    .addPropertyNode("latitude")
                    .addConstraintViolation();
        }

        if (!validLongitude) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Longitude must be between -180 and 180.")
                    .addPropertyNode("longitude")
                    .addConstraintViolation();
        }

        return validLatitude && validLongitude;
    }
}
