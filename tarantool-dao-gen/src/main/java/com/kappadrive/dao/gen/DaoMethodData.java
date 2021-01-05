package com.kappadrive.dao.gen;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.lang.model.element.ExecutableElement;

@Value
@Builder
public class DaoMethodData {
    @NonNull ExecutableElement method;
}
