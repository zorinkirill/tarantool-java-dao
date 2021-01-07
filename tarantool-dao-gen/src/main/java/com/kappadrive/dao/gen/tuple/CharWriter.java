package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleUtil.createSetter;

public class CharWriter implements TupleTypeWriter {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, Character.class) || typeMirror.getKind() == TypeKind.CHAR;
    }

    @Nonnull
    @Override
    public String createEntityGetter(@Nonnull FieldData fieldData, @Nonnull String entity) {
        String getter = TupleTypeWriter.super.createEntityGetter(fieldData, entity);
        if (fieldData.getType().getKind() == TypeKind.CHAR) {
            return "String.valueOf(" + getter + ")";
        }
        return getter + " == null ? null : String.valueOf(" + getter + ")";
    }

    @Nonnull
    @Override
    public CodeBlock createEntitySetter(@Nonnull FieldData fieldData) {
        return createSetter(fieldData, TypeName.get(String.class), ".map(s -> s.charAt(0))");
    }

    @Nonnull
    @Override
    public String createParameterGetter(@Nonnull VariableElement parameter) {
        String value = TupleTypeWriter.super.createParameterGetter(parameter);
        if (parameter.asType().getKind() == TypeKind.CHAR) {
            return "String.valueOf(" + value + ")";
        }
        return value + " == null ? null : String.valueOf(" + value + ")";
    }
}
