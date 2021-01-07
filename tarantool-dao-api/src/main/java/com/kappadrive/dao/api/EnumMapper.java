package com.kappadrive.dao.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks field or method in enum to be used as database value
 * instead default <code>.name()</code>.
 * If field is annotated, there should be as well default getter of same type.
 * If there are multiple annotations present for same enum, behavior is not predicted.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface EnumMapper {
}
