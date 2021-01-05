package com.kappadrive.dao.gen;

import lombok.Builder;
import lombok.Value;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

@Value
@Builder
public class FieldData {
    VariableElement field;
    TypeMirror type;
    String getter;
    String setter;
    String name;
    int order;
    boolean key;
}
