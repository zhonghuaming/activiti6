package cn.lucasma.activiti.coreapi;

import cn.lucasma.activiti.mapper.MyCustomMapper;
import org.activiti.engine.ManagementService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.runtime.DeadLetterJobQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.SuspendedJobQuery;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 管理服务：ManagementService
 * job任务管理
 * 数据库相关通用操作
 * 执行流程引擎命令(Command)
 * <p>
 * job任务查询
 * JonQuery 查询一般工作
 * TimerJobQuery 查询定时工作
 * SuspendEdJobQuery 查询中断工作
 * DeadLetterJobQuery 查询无法执行的工作（任务重试执行3次还是无法完成）
 * <p>
 * 数据库相关操作
 * 查询表结构元数据（TableMetaData）
 * 通用表查询（TablePageQuery）
 * 执行自定义的sql查询（executeCustomSql）
 */
public class ManagementServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ManagementServiceTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti_job.cfg.xml");

    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testJobQuery() {
        ManagementService managementService = activitiRule.getManagementService();

        List<Job> timerJobList = managementService.createTimerJobQuery().listPage(0, 100);
        for (Job timerJob : timerJobList) {
            logger.info("timerJob = {}", timerJob);
        }

        JobQuery jobQuery = managementService.createJobQuery();

        SuspendedJobQuery suspendedJobQuery = managementService.createSuspendedJobQuery();
        DeadLetterJobQuery deadLetterJobQuery = managementService.createDeadLetterJobQuery();
    }


    @Test
    @Deployment(resources = {"my-process-job.bpmn20.xml"})
    public void testTablePageQuery() {
        ManagementService managementService = activitiRule.getManagementService();
        TablePage tablePage = managementService.createTablePageQuery()
                //根据流程实体对象获取表名称 managementService.getTableName(流程实体.class)
                .tableName(managementService.getTableName(ProcessDefinitionEntity.class))
                .listPage(0, 100);

        List<Map<String, Object>> rows = tablePage.getRows();
        for (Map<String, Object> row : rows) {
            logger.info("row = {}", row);
        }

    }

    // 自定义 sql
    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testCustomSql() {
        activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
        ManagementService managementService = activitiRule.getManagementService();
        List<Map<String, Object>> mapList = managementService
                .executeCustomSql(new AbstractCustomSqlExecution<MyCustomMapper
                        , List<Map<String, Object>>>(MyCustomMapper.class) {
                    @Override
                    public List<Map<String, Object>> execute(MyCustomMapper o) {
                        return o.findAll();
                    }
                });

        for (Map<String, Object> map : mapList) {
            logger.info("map = {}", map);
        }
    }

    @Test
    @Deployment(resources = {"my-process.bpmn20.xml"})
    public void testCommand() {
        activitiRule.getRuntimeService().startProcessInstanceByKey("my-process");
        ManagementService managementService = activitiRule.getManagementService();

        ProcessDefinitionEntity processDefinitionEntity = managementService.executeCommand(new Command<ProcessDefinitionEntity>() {

            @Override
            public ProcessDefinitionEntity execute(CommandContext commandContext) {
                ProcessDefinitionEntity processDefinitionEntity = commandContext.getProcessDefinitionEntityManager()
                        .findLatestProcessDefinitionByKey("my-process");
                return processDefinitionEntity;
            }
        });

        logger.info("processDefinitionEntity = {}", processDefinitionEntity);
    }

}
