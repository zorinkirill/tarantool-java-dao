package com.kappadrive.dao.test;

import com.kappadrive.dao.api.Delete;
import com.kappadrive.dao.api.Operation.Add;
import com.kappadrive.dao.api.Operation.Assign;
import com.kappadrive.dao.api.Operation.Sub;
import com.kappadrive.dao.api.Replace;
import com.kappadrive.dao.api.Select;
import com.kappadrive.dao.api.Select.Index;
import com.kappadrive.dao.api.Select.Limit;
import com.kappadrive.dao.api.Select.Offset;
import com.kappadrive.dao.api.Update;
import com.kappadrive.dao.api.Upsert;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static com.kappadrive.dao.api.Select.Iterator.REQ;

public interface BaseDao extends Dao<TestEntity> {

    @Nonnull
    @Index("primary")
    @Select(REQ)
    Optional<TestEntity> getAllByIdDesc(long id);

    @Nonnull
    @Index("by_type")
    @Select
    List<TestEntity> getByType(UserType type, @Limit int limit);

    @Nonnull
    @Index("by_name_and_age")
    List<TestEntity> findAllByNameAndAge(Integer age, @Offset int offset, String name);

    @Nonnull
    @Delete
    Optional<TestEntity> remove(Long id);

    @Nonnull
    TestEntity replace(TestEntity entity);

    @Nonnull
    @Replace
    TestEntity update(TestEntity entity);

    @Nonnull
    TestEntity insert(TestEntity entity);

    void updateType(Long id, @Assign UserType type);

    @Nonnull
    @Update
    Optional<TestEntity> increaseAge(Long id, @Add Integer age);

    void upsertAge(TestEntity entity, @Sub Integer age);

    @Upsert
    void decreaseAge(TestEntity entity, @Sub Integer age);
}
