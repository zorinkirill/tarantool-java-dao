package com.kappadrive.dao.gen;

import com.squareup.javapoet.CodeBlock;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class StatementBuilder {

    private final StringBuilder stringBuilder = new StringBuilder();
    private final List<Object> params = new ArrayList<>();

    @Nonnull
    public StatementBuilder append(@Nonnull String string, @Nonnull Object... params) {
        stringBuilder.append(string);
        this.params.addAll(Arrays.asList(params));
        return this;
    }

}
