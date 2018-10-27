package cn.lucasma.activiti;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author 邓仁波
 * @date 2018-7-23
 */
@Slf4j
public class DemoMain {
    public static void main(String[] args) throws ParseException {

        //创建流程引擎
        ProcessEngine processEngine = getProcessEngine();

        //部署流程定义文件
        ProcessDefinition processDefinition = getProcessDefinition(processEngine);

        //启动运行流程
        String processDefinitionKey = getProcessInstance(processEngine, processDefinition);


        //处理流程任务
        handleTask(processEngine, processDefinitionKey);
    }

    /**
     * 处理流程任务
     *
     * @param processEngine
     * @param processDefinitionKey
     * @throws ParseException
     */
    private static void handleTask(ProcessEngine processEngine, String processDefinitionKey) throws ParseException {
        //获取流程实例最新状态
        List<ProcessInstance> processInstanceList = processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .list();
        //控制台输入
        Scanner scanner = new Scanner(System.in);
        //如果流程实例不为空，且没有结束
        while (processInstanceList != null && processInstanceList.size() > 0) {
            //处理流程任务
            TaskService taskService = processEngine.getTaskService();
            //获取任务列表
            List<Task> list = taskService.createTaskQuery().list();
            log.info("待处理任务数量 {}", list.size());
            for (Task task : list) {
                log.info("待处理任务 {}", task.getName());
                //输入表单数据
                Map<String, Object> variables = getStringObjectMap(processEngine, scanner, task);
                //提交流程
                taskService.complete(task.getId(), variables);
                //获取流程实例最新状态
                processInstanceList = processEngine.getRuntimeService()
                        .createProcessInstanceQuery()
                        .processDefinitionKey(processDefinitionKey)
                        .list();
            }
        }
        scanner.close();
    }

    /**
     * 输入表单数据
     *
     * @param processEngine
     * @param scanner
     * @param task
     * @return
     * @throws ParseException
     */
    private static Map<String, Object> getStringObjectMap(ProcessEngine processEngine, Scanner scanner, Task task) throws ParseException {
        FormService formService = processEngine.getFormService();
        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> formProperties = taskFormData.getFormProperties();
//        //输出表单key value
//        for (FormProperty formProperty : formProperties) {
//            log.info(formProperty.getId()+":"+formProperty.getValue());
//        }
        //表单数据
        Map<String, Object> variables = Maps.newHashMap();
        //循环输入数据
        for (FormProperty formProperty : formProperties) {
            String line = "";
            //判断数据类型
            if (StringFormType.class.isInstance(formProperty.getType())) {
                //字符串类型
                log.info("请输入 {}", formProperty.getName());
                line = scanner.nextLine();
                variables.put(formProperty.getId(), line);
            } else if (DateFormType.class.isInstance(formProperty.getType())) {
                //日期类型
                log.info("请输入 {} 格式(yyyy-MM-dd)", formProperty.getName());
                line = scanner.nextLine();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(line);
                variables.put(formProperty.getId(), date);
            } else {
                //其他类型
                log.info("{} 类型暂不支持", formProperty.getType());
            }
            log.info("您输入的内容是 {}", line);
        }
        return variables;
    }

    private static String getProcessInstance(ProcessEngine processEngine, ProcessDefinition processDefinition) {
        //管理流程实例和执行对象，也就是表示正在执行的操作
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //按照部署流程id启动流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

        //流程实例的流程定义的关key 对应bpmn process id 属性
        String processDefinitionKey = processInstance.getProcessDefinitionKey();
        log.info("启动流程key {}", processDefinitionKey);
        //流程定义id
        log.info("启动流程id {}", processInstance.getProcessDefinitionId());
        //流程实例的流程定义的关key 对应bpmn process name 属性
        log.info("启动流程name {}", processInstance.getProcessDefinitionName());
        return processDefinitionKey;
    }

    private static ProcessDefinition getProcessDefinition(ProcessEngine processEngine) {
        //获取流程定义和部署相关的Service
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //创建部署对象
        DeploymentBuilder deployment = repositoryService.createDeployment();

        //部署
        DeploymentBuilder deploymentBuilder = deployment.addClasspathResource("second_approve.bpmn20.xml");
        Deployment deploy = deploymentBuilder.deploy();

        //部署流程id
        String id = deploy.getId();
        log.info("流程部署id {}", id);

        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                //根据id查询
                .deploymentId(id)
                //执行查询并返回结果实体，如果没有实体与查询条件匹配，则返回null。
                .singleResult();

        log.info("流程定义文件 {},流程定义id {}", processDefinition.getName(), processDefinition.getId());
        return processDefinition;
    }

    private static ProcessEngine getProcessEngine() {
        ProcessEngineConfiguration configuration = ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration();
        //获得流程引擎
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //流程引擎名称
        String name = processEngine.getName();
        //流程引擎版本
        String version = ProcessEngine.VERSION;
        log.info("流程引擎名称{},版本{}", name, version);

        return processEngine;
    }
}
