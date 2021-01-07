package com.kappadrive.dao.test;

import com.kappadrive.dao.api.EnumMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Tax {

    LOW('l'),
    HIGH('h'),
    ;

    @EnumMapper
    private final char value;
}
