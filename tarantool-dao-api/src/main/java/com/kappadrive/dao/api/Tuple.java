package com.kappadrive.dao.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holder for Tuple field markers.
 * Used to properly configure entity class for DAO generation.
 * <p>
 * Currently annotation is disabled.
 */
public @interface Tuple {

    /**
     * Marks particular field to be used as part of PrimaryKey.
     * If there are multiple fields marked as PK on same entity,
     * they will be sorted according to natural order or <code>Order</code> annotation.
     * <p>
     * Should be at least 1 <code>Id</code> field in entity.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Key {
    }

    /**
     * Sets order value for particular field in entity.
     * It is either all not ignored fields should have <code>Order</code>
     * or no fields should have it. In last case, natural order will be used.
     * <p>
     * If <code>Order</code> annotation is used, there should be no duplicates between
     * fields, no negative or zero value or value exceeding total number of not ignored fields.
     * <p>
     * Values should start from 1.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Order {

        /**
         * Returns field position in tuple.
         *
         * @return field position in tuple.
         */
        int value();
    }

    /**
     * Marks field to be completely ignored during DAO generation
     * and to be not considered as part of entity/tuple in Tarantool.
     */
    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Ignore {
    }
}
