package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.FieldData;
import com.kappadrive.dao.gen.util.GenerateUtil;
import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;

import static com.kappadrive.dao.gen.tuple.TupleVisitor.createSetter;

public class StringVisitor implements TupleTypeVisitor {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, String.class);
    }

    @Nonnull
    @Override
    public CodeBlock createEntitySetter(@Nonnull FieldData fieldData) {
        return createSetter(fieldData, String.class, "");
    }
}
