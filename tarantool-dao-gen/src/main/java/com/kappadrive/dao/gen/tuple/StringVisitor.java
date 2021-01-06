package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.util.GenerateUtil;

import javax.annotation.Nonnull;
import javax.lang.model.type.TypeMirror;

class StringVisitor implements TupleTypeVisitor {
    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.hasType(typeMirror, String.class);
    }
}
