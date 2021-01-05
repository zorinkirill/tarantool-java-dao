package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.TupleImplData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleVisitor.createSetter;
import static com.kappadrive.dao.gen.util.GenerateUtil.TUPLE;

public class EnumVisitor implements TupleTypeVisitor {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.isEnum(typeMirror);
    }

    @Nonnull
    @Override
    public CodeBlock createTupleReturn(@Nonnull TypeMirror typeMirror, @Nonnull String value) {
        return CodeBlock.of("return $T.valueOf(($T) $L)", TypeName.get(typeMirror), String.class, value);
    }

    @Nonnull
    @Override
    public CodeBlock createTupleOptionalReturn(@Nonnull TypeMirror typeMirror, @Nonnull String value) {
        return TupleVisitor.createOptionalReturn(value, String.class, ".map($T::valueOf)", TypeName.get(typeMirror));
    }

    @Nonnull
    @Override
    public CodeBlock createTupleSetter(@Nonnull TupleImplData.FieldData fieldData) {
        if (fieldData.isOptional()) {
            return CodeBlock.of("this.$L.set($L, $L == null ? null : $L.name())", TUPLE, fieldData.getOrder(), fieldData.getName(), fieldData.getName());
        }
        return CodeBlock.of("this.$L.set($L, $L.name())", TUPLE, fieldData.getOrder(), fieldData.getName());
    }

    @Nonnull
    @Override
    public String createEntityGetter(@Nonnull FieldData fieldData, @Nonnull String entity) {
        String getter = TupleTypeVisitor.super.createEntityGetter(fieldData, entity);
        return getter + " == null ? null : " + getter + ".name()";
    }

    @Nonnull
    @Override
    public CodeBlock createEntitySetter(@Nonnull FieldData fieldData) {
        return createSetter(fieldData, String.class, ".map($T::valueOf)", fieldData.getType());
    }

    @Nonnull
    @Override
    public String createParameterGetter(@Nonnull VariableElement parameter) {
        String value = TupleTypeVisitor.super.createParameterGetter(parameter);
        return value + " == null ? null : " + value + ".name()";
    }
}
