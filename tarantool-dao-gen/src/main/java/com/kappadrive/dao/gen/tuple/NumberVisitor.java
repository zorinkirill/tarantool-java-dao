package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleVisitor.createSetter;

@RequiredArgsConstructor
abstract class NumberVisitor implements TupleTypeVisitor {

    private final Class<?> objectClass;
    private final TypeKind primitiveKind;
    private final String numberMethod;

    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, objectClass) || typeMirror.getKind() == primitiveKind;
    }

    @Nonnull
    @Override
    public CodeBlock createTupleReturn(@Nonnull TypeMirror typeMirror, @Nonnull String value) {
        return CodeBlock.of("return (($T) $L)." + numberMethod + "()", Number.class, value);
    }

    @Nonnull
    @Override
    public CodeBlock createTupleOptionalReturn(@Nonnull TypeMirror typeMirror, @Nonnull String value) {
        return TupleVisitor.createOptionalReturn(value, Number.class, ".map($T::" + numberMethod + ")", Number.class);
    }

    @Nonnull
    @Override
    public CodeBlock createEntitySetter(@Nonnull FieldData fieldData) {
        return createSetter(fieldData, Number.class, ".map($T::" + numberMethod + ")", Number.class);
    }

    static class LongVisitor extends NumberVisitor {
        public LongVisitor() {
            super(Long.class, TypeKind.LONG, "longValue");
        }
    }

    static class IntVisitor extends NumberVisitor {
        public IntVisitor() {
            super(Integer.class, TypeKind.INT, "intValue");
        }
    }

    static class ShortVisitor extends NumberVisitor {
        public ShortVisitor() {
            super(Short.class, TypeKind.SHORT, "shortValue");
        }
    }

    static class ByteVisitor extends NumberVisitor {
        public ByteVisitor() {
            super(Byte.class, TypeKind.BYTE, "byteValue");
        }
    }

    static class DoubleVisitor extends NumberVisitor {
        public DoubleVisitor() {
            super(Double.class, TypeKind.DOUBLE, "doubleValue");
        }
    }

    static class FloatVisitor extends NumberVisitor {
        public FloatVisitor() {
            super(Float.class, TypeKind.FLOAT, "floatValue");
        }
    }
}
