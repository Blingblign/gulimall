package com.zzclearning.common.validator.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author bling
 * @create 2022-10-27 10:39
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue,Integer> {
    private HashSet<Integer> set = new HashSet<>();
    @Override
    public void initialize(ListValue constraintAnnotation) {

        int[] values = constraintAnnotation.value();
        for (int value : values) {
            set.add(value);
        }

    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(value);
    }
}
