package com.kappadrive.dao.gen;

import lombok.Builder;
import lombok.Value;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;

@Value
@Builder
public class TupleImplData {
    String packageName;
    TypeElement interfaceElement;
    DeclaredType interfaceType;
    String implName;
    Collection<FieldData> methods;
    String builderInterfaceName;
    ExecutableElement toBuilder;

    @Value
    @Builder
    public static class FieldData {
        String name;
        ExecutableElement method;
        int order;
        boolean isOptional;
        TypeMirror type;
    }
}
