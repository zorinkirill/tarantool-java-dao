package com.kappadrive.dao.api;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Main DAO generation annotation.
 * Triggers annotation processor to generate DAO from given interface
 * with specified parameters.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface TarantoolDao {

    /**
     * Returns DAO Entity type. Expected as a regular pojo with annotated fields.
     * Entity should be mutable and have getters and setters.
     *
     * @return DAO entity type.
     */
    Class<?> value();

    /**
     * Annotation with DAO customizations.
     * Can be applied for particular DAO interface or
     * for package for all DAOs in same package as default.
     * Overrides for particular DAO still possible.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.PACKAGE})
    @interface Style {

        /**
         * Returns Annotation classes which should be added
         * to generated DAO implementation.
         * Doesn't support specifying any values for such annotations.
         *
         * @return implementation custom annotations.
         */
        Class<? extends Annotation>[] addAnnotations() default {};
    }
}
