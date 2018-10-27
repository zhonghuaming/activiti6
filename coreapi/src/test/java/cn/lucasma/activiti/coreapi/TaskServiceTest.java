package cn.lucasma.activiti.coreapi;

import com.google.common.collect.Maps;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.*;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * 任务管理服务
 * 对用户任务（UserTask）管理和流程的控制
 * 设置用户任务（UserTask）的权限消息（拥有者，候选人，办理人）
 * 针对用户任务添加任务附件，任务评论和事件记录
 * <p>
 * TaskService对task管理与流程控制：
 * Task对象的创建，删除
 * 查询Task，并驱动Task节点完成执行
 * Task相关参数变量（variable）设置
 * <p>
 * TaskService设置Task权限消息：
 * 候选用户（candidateUser）和候选组（candidateGroup）
 * 指定拥有人（Owner）和办理人（Assignee）
 * 通过claim设置办理人
 * <p>
 * TaskService设置Task附加信息：
 * 任务附件（Attachment）创建与查询
 * 任务评论（Comment）创建与查询
 * 事件记录（Event）创建与查询
 *
 */
public class TaskServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(TaskServiceTest.class);


    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskService() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");
        activitiRule.getRuntimeService()
                .startProcessInstanceByKey("my-process", variables);
        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().singleResult();
        logger.info("task = {}", ToStringBuilder.reflectionToString(task, ToStringStyle.JSON_STYLE));
        logger.info("task.description = {}", task.getDescription());

        //设置普通变量
        taskService.setVariable(task.getId(), "key1", "value1");
        //设置本地变量
        taskService.setVariableLocal(task.getId(), "localKey1", "localValue1");

        //以获取到普通变量和本地变量
        Map<String, Object> taskServiceVariables = taskService.getVariables(task.getId());
        //只能获取到本地变量
        Map<String, Object> taskServiceVariablesLocal = taskService.getVariablesLocal(task.getId());
        //只能获取到普通变量
        Map<String, Object> processVariables = activitiRule.getRuntimeService().getVariables(task.getExecutionId());

        logger.info("taskServiceVariables = {}", taskServiceVariables);
        logger.info("taskServiceVariablesLocal = {}", taskServiceVariablesLocal);
        logger.info("processVariables = {}", processVariables);

        Map<String, Object> completeVar = Maps.newConcurrentMap();
        completeVar.put("cKey1", "cValue1");
        //驱动流程向下个节点
        taskService.complete(task.getId(), completeVar);

        Task task1 = taskService.createTaskQuery().taskId(task.getId()).singleResult();
        logger.info("task1 = {}", task1);

    }


    /**
     * 任务处理人测试
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskServiceUser() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");
        activitiRule.getRuntimeService()
                .startProcessInstanceByKey("my-process", variables);
        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().singleResult();
        logger.info("task = {}", ToStringBuilder.reflectionToString(task, ToStringStyle.JSON_STYLE));
        logger.info("task.description = {}", task.getDescription());

        //一般owner为流程发起人
        taskService.setOwner(task.getId(), "user1");
        //设置代办人 不推荐 没有进行校验（如果原来有指定人 原有代办人会莫名其妙的发现这个任务不是他代办的了）
//        taskService.setAssignee(task.getId(),"lucas");

        //查询候选人里面有lucas但是没指定代办人的task
        List<Task> taskList = taskService
                .createTaskQuery()
                .taskCandidateUser("lucas")
                .taskUnassigned().listPage(0, 100);

        for (Task task1 : taskList) {
            try {
                //设置代办人 推荐  当已经有指定代办人时，会抛出异常
                taskService.claim(task1.getId(), "lucas");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        //用户和task构建的关系
        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());
        for (IdentityLink identityLink : identityLinksForTask) {
            logger.info("identityLink = {}", identityLink);
        }

        //查询lucas为代办人的任务
        List<Task> lucass = taskService
                .createTaskQuery()
                .taskAssignee("lucas")
                .listPage(0, 100);

        for (Task lucas : lucass) {
            //可以指定变量
            Map<String, Object> vars = Maps.newHashMap();
            vars.put("ckey1", "cvalue1");
            taskService.complete(lucas.getId(), vars);
        }

        lucass = taskService.createTaskQuery().taskAssignee("lucas").listPage(0, 100);
        logger.info("是否存在 {}", !CollectionUtils.isEmpty(lucass));
    }

    /**
     * 附件测试
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskAttachment() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");
        activitiRule.getRuntimeService()
                .startProcessInstanceByKey("my-process", variables);
        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().singleResult();
        //添加附件 类型:url 名字:name 描述:desc url:url/test.png
        taskService.createAttachment("url", task.getId(),
                task.getProcessInstanceId(), "name",
                "desc", "/url/test.png");
        //附加查询
        List<Attachment> taskAttachments = taskService.getTaskAttachments(task.getId());
        for (Attachment taskAttachment : taskAttachments) {
            logger.info("taskAttachment = {}", ToStringBuilder.reflectionToString(taskAttachment, ToStringStyle.JSON_STYLE));
        }

    }


    /**
     * 任务评论测试
     */
    @Test
    @Deployment(resources = {"my-process-task.bpmn20.xml"})
    public void testTaskComment() {
        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");
        activitiRule.getRuntimeService()
                .startProcessInstanceByKey("my-process", variables);
        TaskService taskService = activitiRule.getTaskService();
        Task task = taskService.createTaskQuery().singleResult();

        //设置发起人 制造事件记录
        taskService.setOwner(task.getId(), "user1");
        //设置代办人 不推荐 没有进行校验（如果原来有指定人 原有代办人会莫名其妙的发现这个任务不是他代办的了） 制造事件记录
        taskService.setAssignee(task.getId(), "lucas");
        //添加评论
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "record note 1");
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "record note 2");
        //查询评论
        List<Comment> taskComments = taskService.getTaskComments(task.getId());
        for (Comment taskComment : taskComments) {
            logger.info("taskComment = {}", ToStringBuilder.reflectionToString(taskComment, ToStringStyle.JSON_STYLE));
        }

        //事件记录
        List<Event> taskEvents = taskService.getTaskEvents(task.getId());
        for (Event taskEvent : taskEvents) {
            logger.info("taskEvent = {}", ToStringBuilder.reflectionToString(taskEvent, ToStringStyle.JSON_STYLE));
        }

    }
}
