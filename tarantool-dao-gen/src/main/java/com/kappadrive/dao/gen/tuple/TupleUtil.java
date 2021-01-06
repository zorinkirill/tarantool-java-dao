package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.tuple.NumberWriter.ByteWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.DoubleWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.FloatWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.IntWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.LongWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.ShortVisitor;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.kappadrive.dao.gen.util.GenerateUtil.ENTITY;
import static com.kappadrive.dao.gen.util.GenerateUtil.TUPLE;

public final class TupleUtil {

    private TupleUtil() {
    }

    private static final List<TupleTypeWriter> visitors = Arrays.asList(
            new LongWriter(), new IntWriter(), new ShortVisitor(), new ByteWriter(), new DoubleWriter(), new FloatWriter(),
            new BooleanWriter(), new StringWriter(), new EnumWriter(), new ListWriter()
    );

    @Nonnull
    public static TupleTypeWriter findWriter(@Nonnull final TypeMirror type, @Nonnull final GenerateUtil generateUtil) {
        for (TupleTypeWriter tupleType : visitors) {
            if (tupleType.supportType(type, generateUtil)) {
                return tupleType;
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported tuple return type: %s", type));
    }

    public static boolean isSupported(@NonNull final TypeMirror type, @Nonnull final GenerateUtil generateUtil) {
        for (TupleTypeWriter tupleType : visitors) {
            if (tupleType.supportType(type, generateUtil)) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    static CodeBlock createSetter(@Nonnull final FieldData fieldData, @Nonnull final TypeName type, @Nonnull final String mapper, @Nonnull final Object... args) {
        return CodeBlock.builder()
                .add("$L.$L($T.ofNullable($L.get($L)).map(o -> ($T) o)", ENTITY, fieldData.getSetter(), Optional.class, TUPLE, fieldData.getOrder(), type)
                .add(mapper, args)
                .add(".orElse(null))")
                .build();
    }
}
