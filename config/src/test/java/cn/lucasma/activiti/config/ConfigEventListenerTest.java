package cn.lucasma.activiti.config;

import cn.lucasma.activiti.event.CustomEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.delegate.event.impl.ActivitiEventImpl;
import org.activiti.engine.logging.LogMDC;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @Author Lucas Ma
 * <p>
 *     事件监听器测试
 * EventListener 测试
 */
public class ConfigEventListenerTest {
    private static final Logger logger = LoggerFactory.getLogger(ConfigEventListenerTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_eventlistener.cfg.xml");

    @Test
    @Deployment(resources = {"cn/lucasma/activiti/my-process.bpmn20.xml"})
    public void test() {
        // 打开 MDC
        LogMDC.setMDCEnabled(true);
        ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
        assertNotNull(processInstance);

        Task task = activitiRule.getTaskService().createTaskQuery().singleResult();
        assertEquals("Activiti is awesome!", task.getName());
        activitiRule.getTaskService().complete(task.getId());
        //事件监听注册方法三
        activitiRule.getRuntimeService().addEventListener(new CustomEventListener());
        //发送自定义事件
        activitiRule.getRuntimeService().dispatchEvent(new ActivitiEventImpl(ActivitiEventType.CUSTOM));
        
    }

}
