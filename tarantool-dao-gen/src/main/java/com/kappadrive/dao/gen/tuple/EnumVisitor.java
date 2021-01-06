package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleVisitor.createSetter;

class EnumVisitor implements TupleTypeVisitor {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.isEnum(typeMirror);
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
        return createSetter(fieldData, TypeName.get(String.class), ".map($T::valueOf)", fieldData.getType());
    }

    @Nonnull
    @Override
    public String createParameterGetter(@Nonnull VariableElement parameter) {
        String value = TupleTypeVisitor.super.createParameterGetter(parameter);
        return value + " == null ? null : " + value + ".name()";
    }
}
