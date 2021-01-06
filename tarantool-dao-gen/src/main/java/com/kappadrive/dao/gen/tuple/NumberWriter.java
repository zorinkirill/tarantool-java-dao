package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleUtil.createSetter;

@RequiredArgsConstructor
abstract class NumberWriter implements TupleTypeWriter {

    private final Class<?> objectClass;
    private final TypeKind primitiveKind;
    private final String numberMethod;

    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, objectClass) || typeMirror.getKind() == primitiveKind;
    }

    @Nonnull
    @Override
    public CodeBlock createEntitySetter(@Nonnull FieldData fieldData) {
        return createSetter(fieldData, TypeName.get(Number.class), ".map($T::" + numberMethod + ")", Number.class);
    }

    static class LongWriter extends NumberWriter {
        public LongWriter() {
            super(Long.class, TypeKind.LONG, "longValue");
        }
    }

    static class IntWriter extends NumberWriter {
        public IntWriter() {
            super(Integer.class, TypeKind.INT, "intValue");
        }
    }

    static class ShortVisitor extends NumberWriter {
        public ShortVisitor() {
            super(Short.class, TypeKind.SHORT, "shortValue");
        }
    }

    static class ByteWriter extends NumberWriter {
        public ByteWriter() {
            super(Byte.class, TypeKind.BYTE, "byteValue");
        }
    }

    static class DoubleWriter extends NumberWriter {
        public DoubleWriter() {
            super(Double.class, TypeKind.DOUBLE, "doubleValue");
        }
    }

    static class FloatWriter extends NumberWriter {
        public FloatWriter() {
            super(Float.class, TypeKind.FLOAT, "floatValue");
        }
    }
}
