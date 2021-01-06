package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.util.GenerateUtil;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;

class StringWriter implements TupleTypeWriter {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, String.class);
    }
}
