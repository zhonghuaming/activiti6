package cn.lucasma.activiti.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 历史管理服务: HistoryService
 * 管理流程实例结束后的历史数据
 * 构建历史数据的查询对象
 * 根据流程实例id删除流程历史数据
 *
 * HistoryService历史数据实体：
 * HistoricProcessInStance 历史流程实例实体类
 * HistoricVariableInstance 流程或认为变量值的实体
 * HistoricActivityInstance 单个活动节点执行的信息
 * HistoricTaskInstance 用户任务实例的信息
 * HistoricDetail 历史流程活动任务详细信息
 *
 * HistoryService构建历史查询对象
 * create[历史数据实体]Query： 一般查询可以根据属性查询
 * createNative[历史数据实体]Query： 可以根据传统的sql语句查询
 * createProcessInStanceHistoryLogQuery[历史数据实体]Query： 一次只能查询出一个流程实例的对象
 *
 * HistoryService删除历史操作
 * deleteHistoricProcessInStance：删除历史流程实例 会删除掉和实例相关的节点信息、task信息、评论信息、变量信息等通通删除掉
 * deleteHistoricTaskInstance：删除task实例并且会删除掉和task相关的变量等删除掉
 */
public class HistoryServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(HistoryServiceTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_history.cfg.xml");

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testHistoryService() {

        HistoryService historyService = activitiRule.getHistoryService();
        // 流程实例
        ProcessInstanceBuilder processInstanceBuilder = activitiRule.getRuntimeService().createProcessInstanceBuilder();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key0", "value0");
        variables.put("key1", "value1");
        variables.put("key2", "value2");


        Map<String, Object> transientVariables = Maps.newHashMap();
        transientVariables.put("tkey1", "tvalue1");

        ProcessInstance processInstance = processInstanceBuilder
                .processDefinitionKey("my-process")
                .variables(variables)// 持久变量
                .transientVariables(transientVariables).start();// 瞬时变量 不会存到历史库里

        // 修改数据，看是否记录在History里
        activitiRule.getRuntimeService()
                .setVariable(processInstance.getId(), "key1", "value1_1");


        Task task = activitiRule.getTaskService()
                .createTaskQuery()
                .processInstanceId(processInstance.getId()).singleResult();
//        activitiRule.getTaskService().complete(task.getId(),variables);
        Map<String, String> properties = Maps.newHashMap();
        properties.put("fKey1", "fValue1");
        properties.put("key2", "value_2_2");

        // 通过表单提交数据
        activitiRule.getFormService().submitTaskFormData(task.getId(), properties);




        // 流程实例
        List<HistoricProcessInstance> historicProcessInstances = historyService
                .createHistoricProcessInstanceQuery()
                .listPage(0, 100);
        
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            logger.info("historicProcessInstance = {}", ToStringBuilder
                    .reflectionToString(historicProcessInstance, ToStringStyle.JSON_STYLE));
        }

        // 流程节点
        List<HistoricActivityInstance> historicActivityInstances = historyService
                .createHistoricActivityInstanceQuery()
                .listPage(0, 100);

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            logger.info("historicActivityInstance = {}",historicActivityInstance);
        }

        // 流程任务
        List<HistoricTaskInstance> historicTaskInstances = historyService
                .createHistoricTaskInstanceQuery()
                .listPage(0, 100);

        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            logger.info("historicTaskInstance = {}",ToStringBuilder
                    .reflectionToString(historicTaskInstance,ToStringStyle.JSON_STYLE));
        }

        // 流程变量
        List<HistoricVariableInstance> historicVariableInstances = historyService
                .createHistoricVariableInstanceQuery()
                .listPage(0, 100);
        for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
            logger.info("historicVariableInstance = {}",historicVariableInstance);
        }

        // 流程详情
        List<HistoricDetail> historicDetails = historyService
                .createHistoricDetailQuery()
                .listPage(0, 100);
        for (HistoricDetail historicDetail : historicDetails) {
            logger.info("historicDetail = {} ",historicDetail);
        }

        // 流程历史日志信息
        ProcessInstanceHistoryLog processInstanceHistoryLog = historyService
                .createProcessInstanceHistoryLogQuery(processInstance.getId())
                .includeVariables() //includexxx  包含xxx类型的数据
                .includeFormProperties()
                .includeComments()
                .includeTasks()
                .includeActivities()
                .includeVariableUpdates().singleResult();

        List<HistoricData> historicDataList = processInstanceHistoryLog.getHistoricData();
        for (HistoricData historicData : historicDataList) {
            logger.info("historicData = {}",historicData);
        }

        // 删除流程实例
        historyService.deleteHistoricProcessInstance(processInstance.getId());

        // 查询一次流程实例 查看结果还存不存在
        HistoricProcessInstance historicProcessInstance = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceId(processInstance.getId()).singleResult();

        logger.info("historicProcessInstance = {}",historicProcessInstance);

    }
}
