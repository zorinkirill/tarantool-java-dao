package com.kappadrive.dao.gen;

import com.squareup.javapoet.MethodSpec;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

@Value
@Builder
public class DaoImplData {
    @NonNull String packageName;
    @NonNull TypeElement interfaceElement;
    @NonNull DeclaredType interfaceType;
    @NonNull String implName;
    @NonNull Collection<DaoMethodData> daoMethods;
    @NonNull EntityData entity;
    @NonNull String space;
    @NonNull List<TypeElement> annotations;

    @NonNull MethodSpec toTuple;
    @NonNull MethodSpec toKey;
    @NonNull MethodSpec toEntity;
}
