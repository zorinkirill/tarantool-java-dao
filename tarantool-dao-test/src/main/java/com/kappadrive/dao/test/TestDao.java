package com.kappadrive.dao.test;

import com.kappadrive.dao.api.Select;
import com.kappadrive.dao.api.Select.Index;
import com.kappadrive.dao.api.Select.Limit;
import com.kappadrive.dao.api.TarantoolDao;

import javax.annotation.Nonnull;
import java.util.List;

@TarantoolDao(TestEntity.class)
public interface TestDao extends BaseDao {

    @Nonnull
    @Override
    @Index("other")
    @Select
    List<TestEntity> getByType(UserType type, @Limit int limit);

    @Nonnull
    @Index("by_tax_and_age")
    List<TestEntity> findAllByTax(Tax tax, Integer age);
}
