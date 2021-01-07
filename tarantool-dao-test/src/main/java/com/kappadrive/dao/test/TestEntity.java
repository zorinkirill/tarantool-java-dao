package com.kappadrive.dao.test;

import com.kappadrive.dao.api.Space;
import com.kappadrive.dao.api.Tuple.Ignore;
import com.kappadrive.dao.api.Tuple.Key;
import com.kappadrive.dao.api.Tuple.Order;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Space("entities")
public class TestEntity {

    @Order(3)
    @NonNull
    private String name;
    @Order(2)
    private Integer age;
    @Order(1)
    @NonNull
    @Key
    private Long id;
    @Order(4)
    @NonNull
    private UserType type;
    @Order(5)
    private UserType previousType;
    @Ignore
    private String ignored;
    @Order(6)
    @NonNull
    private List<Long> values = new ArrayList<>();
    @Order(7)
    private boolean active;
    @Order(8)
    private char sex;
}
