package cn.lucasma.activiti.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 流程运行控制服务
 * 启动流程以及对流程数据的控制
 * 流程实例（ProcessInstance）和执行流（Execution）查询
 * 触发流程操作、接受消息和信号
 * <p>
 * 启动流程与变量管理：
 * 启动流程的常用方式（id,key,message）
 * 启动流程可选参数（businessKey，variables，tenantId） businessKey一般保存业务id
 * 变量（variables）的设置和获取
 * <p>
 * 流程实例于执行流：
 * 流程实例（ProcessInstance）表示一次工作流业务和数据实体
 * 执行流（Execution）表示流程实例中具体的执行路径
 * 流程实例接口继承于执行流
 * <p>
 * 流程触发：
 * 使用trigger触发ReceiveTask节点
 * 触发信号捕获事件signalEventReceived
 * 触发消息捕获事件messageEventReceived
 */
public class RunTimeServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(RunTimeServiceTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    /**
     * 通过 key 启动  默认使用流程最新的版本
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testStartPrecess() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        设置启动参数
        Map<String, Object> map = Maps.newHashMap();
        map.put("key1", "val1");
        // 流程实例对象
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", map);
        logger.info("processInstance = {}", processInstance);
    }


    /**
     * 通过 流程 ID 启动
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testStartPrecessById() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
//        查询流程定义对象
        ProcessDefinition processDefinition = activitiRule.getRepositoryService()
                .createProcessDefinitionQuery()
                .singleResult();
        Map<String, Object> map = Maps.newHashMap();
        map.put("key1", "val1");
        // 流程实例对象
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), map);
        logger.info("processInstance = {}", processInstance);
    }


    /**
     * 通过 ProcessInstanceBuilder 启动
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testPrecessInstanceBuilder() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> map = Maps.newHashMap();
        map.put("key1", "val1");

        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
        ProcessInstance processInstance = processInstanceBuilder
                .businessKey("businessKey001")
//                设置要启动的流程的定义key
                .processDefinitionKey("my-process")
//                设置启动变量
                .variables(map)
                .start();
        logger.info("processInstance = {}", processInstance);
    }


    /**
     * 流程变量测试
     * <p>
     * 流程实例（ProcessInstance）与执行流（Execution）的关系
     * ProcessInstance 表示一次工作流业务的实体，当每次启动流程的时候，生成一个流程实例
     * Execution 表示流程实例中具体的执行路径，如果是说一个简单的流程，其只有一条线的话， 可以理解为，每一次流程实例就对应一个执行流
     * 这种情况下流程实例和执行流对应的 ID 是一致的。
     * ProcessInstance 继承 Execution
     */

    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testVariables() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> map = Maps.newHashMap();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");
        // 流程实例对象
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", map);
        logger.info("processInstance = {}", processInstance);
        // 增加新的变量
        runtimeService.setVariable(processInstance.getId(), "key4", "val4");
        // 修改旧的变量
        runtimeService.setVariable(processInstance.getId(), "key1", "val1-new");
        // 获取变量
        Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
        logger.info("variables = {}", variables);
    }


    /**
     * 流程实例的查询
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testProcessInstanceQuery() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> map = Maps.newHashMap();

        // 流程实例对象
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", map);
        logger.info("processInstance = {}", processInstance);

        ProcessInstance processInstance1 = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstance.getId()).singleResult();

    }

    /**
     * 流程执行对象的查询
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testExecutionQuery() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        Map<String, Object> map = Maps.newHashMap();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", map);
        logger.info("processInstance = {}", processInstance);

        List<Execution> executionList = runtimeService.createExecutionQuery()
                .listPage(0, 100);
        for (Execution execution : executionList) {
            logger.info("execution = {}", execution);
        }
    }


    /**
     * 使用trigger触发ReceiveTask节点
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-trigger.bpmn20.xml"})
    public void testTrigger() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService.createExecutionQuery()
                .activityId("someTask")
                .singleResult();
        logger.info("execution = {}", execution);

        runtimeService.trigger(execution.getId());
        execution = runtimeService.createExecutionQuery()
                .activityId("someTask")
                .singleResult();
        logger.info("execution = {}", execution);
    }

    /**
     * 信号触发
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-signal-received.bpmn20.xml"})
    public void testSignalEventReceived() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService.createExecutionQuery()
                .signalEventSubscriptionName("my-signal")
                .singleResult();

        logger.info("execution = {}", execution);

        runtimeService.signalEventReceived("my-signal");

        execution = runtimeService.createExecutionQuery()
                .signalEventSubscriptionName("my-signal")
                .singleResult();

        logger.info("execution = {} ", execution);
    }


    /**
     * 消息 触发
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-message-received.bpmn20.xml"})
    public void testMessageEventReceived() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        Execution execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("my-message")
                .singleResult();

        logger.info("execution = {}", execution);

        runtimeService.messageEventReceived("my-message", execution.getId());

        execution = runtimeService.createExecutionQuery()
                .messageEventSubscriptionName("my-message")
                .singleResult();

        logger.info("execution = {} ", execution);

    }

    /**
     * message 启动流程
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process-message.bpmn20.xml"})
    public void testMessageStart() {
        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
//                .startProcessInstanceByKey("my-process");
                .startProcessInstanceByMessage("my-message");

        logger.info("processInstance = {}", processInstance);

    }
}
