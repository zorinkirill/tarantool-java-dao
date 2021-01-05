package com.kappadrive.dao.gen;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nonnull;
import javax.lang.model.type.DeclaredType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Builder
public class EntityData {

    @NonNull DeclaredType type;
    @NonNull List<FieldData> fields;

    @Nonnull
    public Stream<FieldData> fieldsSortedByOrder() {
        return fields.stream().sorted(Comparator.comparingInt(FieldData::getOrder));
    }
}
