package com.alibaba.dataops.server.domain.data.core.dialect.mysq;

import com.alibaba.dataops.server.domain.data.api.enums.CollationEnum;
import com.alibaba.dataops.server.domain.data.core.dialect.common.enums.BaseCollationEnum;

import lombok.Getter;

/**
 * 排序枚举
 *
 * @author Jiaju Zhuang
 */
@Getter
public enum MysqlCollationEnum implements BaseCollationEnum {
    /**
     * ASC
     */
    ASC("A", CollationEnum.ASC),
    /**
     * DESC
     */
    DESC("D", CollationEnum.DESC),
    ;

    final String code;
    final CollationEnum collation;

    MysqlCollationEnum(String code, CollationEnum collation) {
        this.code = code;
        this.collation = collation;
    }

    @Override
    public String getDescription() {
        return getCode();
    }

}
