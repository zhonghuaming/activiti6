package cn.lucasma.activiti.config;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

/**
 * 数据库配置测试
 *
 * @author 邓仁波
 * @date 2018-7-29
 */
@Slf4j
public class ConfigDBTest {
    @Test
    public void testConfig1() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResourceDefault();
        log.info("configuration = {}", configuration);
        ProcessEngine processEngine = configuration.buildProcessEngine();
        log.info("获取流程引擎 {}", processEngine.getName());
        processEngine.close();
    }
    @Test
    public void testConfig2() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResource("activiti_druid.cfg.xml");
        log.info("configuration = {}", configuration);
        ProcessEngine processEngine = configuration.buildProcessEngine();
        log.info("获取流程引擎 {}", processEngine.getName());
        processEngine.close();
    }
}
