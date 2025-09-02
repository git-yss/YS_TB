package org.ys.transaction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * 项目启动初始化数据库
 */
@Service
public class InitDataBase {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitDataBase.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.database-name}")
    private String databaseName;

    // 获取数据库连接URL（不包含具体数据库）
    @Value("${spring.datasource.url-root}")
    private String rootUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * 在应用启动时检查并初始化数据库
     */
    @PostConstruct
    public void checkAndInitDatabase() {
        try {
            // 检查数据库是否存在，如果不存在则创建
            if (!isDatabaseExists()) {
                log.info("数据库不存在，开始执行初始化脚本...");
                executeInitScript();
                log.info("数据库初始化完成");
            }

        } catch (Exception e) {
            throw new RuntimeException("数据库初始化检查失败", e);
        }
    }

    /**
     * 检查数据库是否存在
     * @return 是否存在
     */
    private boolean isDatabaseExists() {
        try {
            // 获取数据库名称（从配置中获取）
            String sql = "SELECT SCHEMA_NAME FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ?";
            List<String> result = jdbcTemplate.queryForList(sql, String.class, databaseName);
            return !result.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
    /**
     * 执行完整的初始化SQL脚本（包含建库和建表语句）
     */
    private void executeInitScript() {

        Connection connection = null;
        try {
            // 使用不指定数据库的连接来执行建库脚本
            connection = DriverManager.getConnection(rootUrl, username, password);
            ClassPathResource resource = new ClassPathResource("sqlScript/ys_tb.sql");
            // 执行SQL脚本
            ScriptUtils.executeSqlScript(connection, resource);
        } catch (Exception e) {
            throw new RuntimeException("执行数据库初始化脚本失败: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    System.err.println("关闭连接时出错: " + e.getMessage());
                }
            }
        }
    }






}
