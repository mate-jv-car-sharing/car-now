package com.example.carsharing.validation.annotation;

import com.example.carsharing.validation.validator.FieldMatchValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = {ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FieldMatchValidator.class})
public @interface FieldMatch {
    String message() default "Fields do not match";
    String firstFieldName();
    String secondFieldName();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
