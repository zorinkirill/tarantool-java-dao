package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeName;

import javax.annotation.Nonnull;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleVisitor.createSetter;

public interface TupleTypeVisitor {

    boolean supportType(@Nonnull final TypeMirror typeMirror, @Nonnull final GenerateUtil generateUtil);

    @Nonnull
    default String createEntityGetter(@Nonnull final FieldData fieldData, @Nonnull final String entity) {
        return String.format("%s.%s()", entity, fieldData.getGetter());
    }

    @Nonnull
    default CodeBlock createEntitySetter(@Nonnull final FieldData fieldData) {
        return createSetter(fieldData, TypeName.get(fieldData.getType()), "");
    }

    @Nonnull
    default String createParameterGetter(@Nonnull final VariableElement parameter) {
        return parameter.getSimpleName().toString();
    }
}
