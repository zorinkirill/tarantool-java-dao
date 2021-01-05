package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.TupleImplData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.util.GenerateUtil.TUPLE;

public interface TupleTypeVisitor {

    boolean supportType(@Nonnull final TypeMirror typeMirror, @Nonnull final GenerateUtil generateUtil);

    @Nonnull
    CodeBlock createTupleReturn(@Nonnull final TypeMirror typeMirror, @Nonnull final String value);

    @Nonnull
    CodeBlock createTupleOptionalReturn(@Nonnull final TypeMirror typeMirror, @Nonnull final String value);

    @Nonnull
    default CodeBlock createTupleSetter(@Nonnull final TupleImplData.FieldData fieldData) {
        return CodeBlock.of("this.$L.set($L, $L)", TUPLE, fieldData.getOrder(), fieldData.getName());
    }

    @Nonnull
    default String createEntityGetter(@Nonnull final FieldData fieldData, @Nonnull final String entity) {
        return String.format("%s.%s()", entity, fieldData.getGetter());
    }

    @Nonnull
    CodeBlock createEntitySetter(@Nonnull final FieldData fieldData);

    @Nonnull
    default String createParameterGetter(@Nonnull final VariableElement parameter) {
        return parameter.getSimpleName().toString();
    }
}
