package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.tuple.NumberWriter.ByteWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.DoubleWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.FloatWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.IntWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.LongWriter;
import com.kappadrive.dao.gen.tuple.NumberWriter.ShortVisitor;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import lombok.NonNull;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;

public final class TupleUtil {

    private TupleUtil() {
    }

    private static final List<TupleTypeWriter> visitors = Arrays.asList(
            new LongWriter(), new IntWriter(), new ShortVisitor(), new ByteWriter(), new DoubleWriter(), new FloatWriter(),
            new BooleanWriter(), new CharWriter(), new StringWriter(), new EnumWriter(), new ListWriter()
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
    static CodeBlock simpleCast(@Nonnull final TypeMirror type) {
        return CodeBlock.of(".map($T.class::cast)", type);
    }

    @Nonnull
    static CodeBlock simpleCast(@Nonnull final Class<?> type) {
        return CodeBlock.of(".map($T.class::cast)", type);
    }

    @Nonnull
    static CodeBlock genericCast(@Nonnull final TypeMirror type) {
        return CodeBlock.of(".map(o -> ($T) o)", type);
    }
}
