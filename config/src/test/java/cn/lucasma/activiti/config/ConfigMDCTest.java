package cn.lucasma.activiti.config;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.logging.LogMDC;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * activitimdc配置测试
 * @author 邓仁波
 * @date 2018-7-29
 */
@Slf4j
public class ConfigMDCTest {
    /**
     * 自动创建流程引擎并且配置
     */
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_mdc.cfg.xml");

    //    public ActivitiRule activitiRule = new ActivitiRule();
    @Test
    //在启动单元测试以前把my-process.bpmn20.xml部署到流程1引擎中
    @Deployment(resources = {"my-process.bpmn20.xml"})
//    @Deployment(resources = {"my-process_mdcerror.bpmn20.xml"})
    public void test() {
        //打开mdc
        LogMDC.setMDCEnabled(true);
        ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
        assertNotNull(processInstance);
        Task task = activitiRule.getTaskService().createTaskQuery().singleResult();

        assertEquals("Activiti is awesome!", task.getName());

    }

}
