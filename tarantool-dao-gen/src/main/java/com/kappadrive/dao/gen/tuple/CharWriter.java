package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleUtil.simpleCast;

public class CharWriter implements TupleTypeWriter {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, Character.class) || typeMirror.getKind() == TypeKind.CHAR;
    }

    @Nonnull
    @Override
    public String createGetter(@Nonnull TypeMirror type, @Nonnull String value, @Nonnull GenerateUtil generateUtil) {
        if (type.getKind() == TypeKind.CHAR) {
            return "String.valueOf(" + value + ")";
        }
        return value + " == null ? null : String.valueOf(" + value + ")";
    }

    @Nonnull
    @Override
    public CodeBlock createOptionalSetter(@Nonnull TypeMirror type, @Nonnull GenerateUtil generateUtil) {
        return simpleCast(String.class).toBuilder()
                .add(".map(s -> s.charAt(0))")
                .build();
    }
}
