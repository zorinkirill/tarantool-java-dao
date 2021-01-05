package com.kappadrive.dao.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Operations for Update/Upsert queries.
 * Should be placed on Parameters of such methods.
 */
public @interface Operation {

    /**
     * Marks that parameter should assigned to related entity field.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface Assign {
    }

    /**
     * Marks that parameter should be added to related entity field.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface Add {
    }

    /**
     * Marks that parameter should be subtracted from related entity field.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface Sub {
    }

    /**
     * Marks that parameter should be applied as AND with related entity field.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface And {
    }

    /**
     * Marks that parameter should be applied as OR with related entity field.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface Or {
    }

    /**
     * Marks that parameter should be applied as XOR with related entity field.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.PARAMETER)
    @interface Xor {
    }
}
