package com.kappadrive.dao.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies DAO space in Tarantool.
 * Can be placed on entity type or directly
 * on particular DAO interface.
 * If both places are presented, DAO one will be used.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Space {

    /**
     * Returns name of space.
     *
     * @return name of space.
     */
    String value();
}
