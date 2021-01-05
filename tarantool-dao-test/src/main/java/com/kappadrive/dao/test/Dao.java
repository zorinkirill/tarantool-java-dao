package com.kappadrive.dao.test;

import com.kappadrive.dao.api.Insert;
import com.kappadrive.dao.api.Select;
import com.kappadrive.dao.api.Select.Index;

import javax.annotation.Nonnull;
import java.util.List;

public interface Dao<T> {

    @Nonnull
    @Index("primary")
    @Select
    List<T> getAll();

    @Nonnull
    @Insert
    T add(T entity);

    void delete(T entity);
}
