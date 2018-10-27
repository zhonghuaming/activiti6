package cn.lucasma.activiti.config;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngineConfiguration;
import org.junit.Test;

/**
 * @author 邓仁波
 * @date 2018-7-28
 * 创建流程引擎配置测试
 */
@Slf4j
public class ConfigTest {
    /**
     * 基于 资源文件去加载
     */
    @Test
    public void testConfig1() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createProcessEngineConfigurationFromResourceDefault();
        log.info("configuration = {}", configuration);
    }

    /**
     * 直接new一个 StandaloneProcessEngineConfiguration 出来
     */
    @Test
    public void testConfig2() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneProcessEngineConfiguration();
        log.info("configuration = {}", configuration);
    }
}
