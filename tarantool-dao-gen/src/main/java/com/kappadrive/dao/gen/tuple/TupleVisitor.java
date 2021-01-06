package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.tuple.NumberVisitor.ByteVisitor;
import com.kappadrive.dao.gen.tuple.NumberVisitor.DoubleVisitor;
import com.kappadrive.dao.gen.tuple.NumberVisitor.FloatVisitor;
import com.kappadrive.dao.gen.tuple.NumberVisitor.IntVisitor;
import com.kappadrive.dao.gen.tuple.NumberVisitor.LongVisitor;
import com.kappadrive.dao.gen.tuple.NumberVisitor.ShortVisitor;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.kappadrive.dao.gen.util.GenerateUtil.ENTITY;
import static com.kappadrive.dao.gen.util.GenerateUtil.TUPLE;

public final class TupleVisitor {

    private TupleVisitor() {
    }

    private static final List<TupleTypeVisitor> visitors = Arrays.asList(
            new LongVisitor(), new IntVisitor(), new ShortVisitor(), new ByteVisitor(), new DoubleVisitor(), new FloatVisitor(),
            new StringVisitor(), new EnumVisitor()
    );

    public static <R> R visit(@Nonnull final TypeMirror type, @Nonnull final GenerateUtil generateUtil, @Nonnull final Function<? super TupleTypeVisitor, ? extends R> creator) {
        for (TupleTypeVisitor tupleType : visitors) {
            if (tupleType.supportType(type, generateUtil)) {
                return creator.apply(tupleType);
            }
        }
        throw new IllegalArgumentException(String.format("Unsupported tuple return type: %s", type));
    }

    @Nonnull
    static CodeBlock createSetter(@Nonnull final FieldData fieldData, @Nonnull final Class<?> type, @Nonnull final String mapper, @Nonnull final Object... args) {
        return CodeBlock.builder()
                .add("$L.$L($T.ofNullable($L.get($L)).map($T.class::cast)", ENTITY, fieldData.getSetter(), Optional.class, TUPLE, fieldData.getOrder(), type)
                .add(mapper, args)
                .add(".orElse(null))")
                .build();
    }
}
