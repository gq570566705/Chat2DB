package com.alibaba.dataops.server.test.domain.data.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.dataops.server.domain.data.api.enums.DbTypeEnum;
import com.alibaba.dataops.server.domain.data.api.model.CellDTO;
import com.alibaba.dataops.server.domain.data.api.model.ExecuteResultDTO;
import com.alibaba.dataops.server.domain.data.api.param.console.ConsoleCreateParam;
import com.alibaba.dataops.server.domain.data.api.param.datasource.DataSourceCreateParam;
import com.alibaba.dataops.server.domain.data.api.param.template.TemplateExecuteParam;
import com.alibaba.dataops.server.domain.data.api.service.ConsoleDataService;
import com.alibaba.dataops.server.domain.data.api.service.DataSourceDataService;
import com.alibaba.dataops.server.domain.data.api.service.JdbcTemplateDataService;
import com.alibaba.dataops.server.domain.data.api.service.TableDataService;
import com.alibaba.dataops.server.test.domain.data.service.dialect.DialectProperties;
import com.alibaba.dataops.server.test.domain.data.utils.TestUtils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 查询测试
 *
 * @author Jiaju Zhuang
 */
@Slf4j
public class JdbcTemplateDataServiceTest {
    /**
     * 表名
     */
    public static final String TABLE_NAME = "DATA_OPS_TEMPLATE_TEST_" + System.currentTimeMillis();
    private final static String STRING = "STR";
    private final static Date DATE = new Date();
    private final static long NUMBER = 1L;

    @Resource
    private DataSourceDataService dataSourceDataService;
    @Resource
    private ConsoleDataService consoleDataService;
    @Autowired
    private List<DialectProperties> dialectPropertiesList;
    @Resource
    private JdbcTemplateDataService jdbcTemplateDataService;
    @Resource
    private TableDataService tableDataService;

    @Test
    @Order(1)
    public void prepare() {
        for (DialectProperties dialectProperties : dialectPropertiesList) {
            DbTypeEnum dbTypeEnum = dialectProperties.getDbType();
            Long dataSourceId = TestUtils.nextLong();
            Long consoleId = TestUtils.nextLong();

            DataSourceCreateParam dataSourceCreateParam = new DataSourceCreateParam();
            dataSourceCreateParam.setDataSourceId(dataSourceId);
            dataSourceCreateParam.setDbType(dbTypeEnum.getCode());
            dataSourceCreateParam.setUrl(dialectProperties.getUrl());
            dataSourceCreateParam.setUsername(dialectProperties.getUsername());
            dataSourceCreateParam.setPassword(dialectProperties.getPassword());
            dataSourceDataService.create(dataSourceCreateParam);

            // 创建控制台
            ConsoleCreateParam consoleCreateParam = new ConsoleCreateParam();
            consoleCreateParam.setDataSourceId(dataSourceId);
            consoleCreateParam.setConsoleId(consoleId);
            consoleCreateParam.setDatabaseName(dialectProperties.getDatabaseName());
            consoleDataService.create(consoleCreateParam);

            // 创建表结构
            TemplateExecuteParam templateQueryParam = new TemplateExecuteParam();
            templateQueryParam.setConsoleId(consoleId);
            templateQueryParam.setDataSourceId(dataSourceId);
            templateQueryParam.setSql(dialectProperties.getCrateTableSql(TABLE_NAME));
            jdbcTemplateDataService.execute(templateQueryParam);

            // 插入
            templateQueryParam = new TemplateExecuteParam();
            templateQueryParam.setConsoleId(consoleId);
            templateQueryParam.setDataSourceId(dataSourceId);
            templateQueryParam.setSql(dialectProperties.getInsertSql(TABLE_NAME, DATE, NUMBER, STRING));
            ExecuteResultDTO executeResult = jdbcTemplateDataService.execute(templateQueryParam).getData();
            Assertions.assertTrue(executeResult.getSuccess(), "查询数据失败");
            Assertions.assertEquals(1, executeResult.getUpdateCount(), "查询数据失败");

            // 查询
            templateQueryParam = new TemplateExecuteParam();
            templateQueryParam.setConsoleId(consoleId);
            templateQueryParam.setDataSourceId(dataSourceId);
            templateQueryParam.setSql(dialectProperties.getSelectSqlById(TABLE_NAME, 1L));
            executeResult = jdbcTemplateDataService.execute(templateQueryParam).getData();
            Assertions.assertTrue(executeResult.getSuccess(), "查询数据失败");
            List<CellDTO> headerList = executeResult.getHeaderList();
            Assertions.assertEquals(1L, headerList.size(), "查询数据失败");
            Assertions.assertEquals("ID", headerList.get(0).getStringValue(), "查询数据失败");

            List<List<CellDTO>> dataList = executeResult.getDataList();
            Assertions.assertEquals(1L, dataList.size(), "查询数据失败");
            List<CellDTO> data1 = dataList.get(0);
            Assertions.assertEquals(BigDecimal.ONE, data1.get(0).getBigDecimalValue(), "查询数据失败");
            Assertions.assertEquals(DATE.getTime(), data1.get(1).getDateValue(), "查询数据失败");
            Assertions.assertEquals(NUMBER, data1.get(2).getBigDecimalValue().longValue(), "查询数据失败");
            Assertions.assertEquals(STRING, data1.get(3).getStringValue(), "查询数据失败");
        }
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void dropTable() {
        for (DialectProperties dialectProperties : dialectPropertiesList) {
            try {
                DbTypeEnum dbTypeEnum = dialectProperties.getDbType();
                Long dataSourceId = TestUtils.nextLong();
                Long consoleId = TestUtils.nextLong();

                DataSourceCreateParam dataSourceCreateParam = new DataSourceCreateParam();
                dataSourceCreateParam.setDataSourceId(dataSourceId);
                dataSourceCreateParam.setDbType(dbTypeEnum.getCode());
                dataSourceCreateParam.setUrl(dialectProperties.getUrl());
                dataSourceCreateParam.setUsername(dialectProperties.getUsername());
                dataSourceCreateParam.setPassword(dialectProperties.getPassword());
                dataSourceDataService.create(dataSourceCreateParam);

                // 创建控制台
                ConsoleCreateParam consoleCreateParam = new ConsoleCreateParam();
                consoleCreateParam.setDataSourceId(dataSourceId);
                consoleCreateParam.setConsoleId(consoleId);
                consoleCreateParam.setDatabaseName(dialectProperties.getDatabaseName());
                consoleDataService.create(consoleCreateParam);

                // 创建表结构
                TemplateExecuteParam templateQueryParam = new TemplateExecuteParam();
                templateQueryParam.setConsoleId(consoleId);
                templateQueryParam.setDataSourceId(dataSourceId);
                templateQueryParam.setSql(dialectProperties.getDropTableSql(TABLE_NAME));
                jdbcTemplateDataService.execute(templateQueryParam);
            } catch (Exception e) {
                log.warn("删除表结构失败.", e);
            }
        }
    }

}
