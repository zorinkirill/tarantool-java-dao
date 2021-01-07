package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleUtil.simpleCast;

public interface TupleTypeWriter {

    boolean supportType(@Nonnull final TypeMirror typeMirror, @Nonnull final GenerateUtil generateUtil);

    @Nonnull
    default String createGetter(@Nonnull final TypeMirror type, @Nonnull final String value, @Nonnull final GenerateUtil generateUtil) {
        return value;
    }

    @Nonnull
    default CodeBlock createOptionalSetter(@Nonnull final TypeMirror type, @Nonnull final GenerateUtil generateUtil) {
        return simpleCast(type);
    }
}
