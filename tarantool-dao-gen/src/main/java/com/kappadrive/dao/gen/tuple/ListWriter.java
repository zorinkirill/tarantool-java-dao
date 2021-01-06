package com.kappadrive.dao.gen.tuple;

import com.kappadrive.dao.gen.util.GenerateUtil;

import javax.annotation.Nonnull;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static com.kappadrive.dao.gen.tuple.TupleUtil.isSupported;

class ListWriter implements TupleTypeWriter {

    @Override
    public boolean supportType(@Nonnull TypeMirror typeMirror, @Nonnull GenerateUtil generateUtil) {
        return generateUtil.isAssignableGeneric(typeMirror, List.class)
                && isSupported(((DeclaredType) typeMirror).getTypeArguments().get(0), generateUtil);
    }
}
