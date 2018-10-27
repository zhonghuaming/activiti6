package cn.lucasma.activiti.dbentity;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author Lucas Ma
 */
public class DbRepositoryTest {
    private static final Logger logger = LoggerFactory.getLogger(DbRepositoryTest.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("activiti-mysql.cfg.xml");

    @Test
    public void testDeploy() {
        activitiRule.getRepositoryService().createDeployment()
                .name("二次审批流程")
                .addClasspathResource("second_approve.bpmn20.xml")
                .deploy();
    }

    @Test
    public void testSuspend() {
        RepositoryService repositoryService = activitiRule.getRepositoryService();
        //流程挂起
        repositoryService.suspendProcessDefinitionById("second_approve:2:7504");
        //判断流程是否被挂起
        boolean suspended = repositoryService.isProcessDefinitionSuspended("second_approve:2:7504");
        logger.info("suspended = {}", suspended);

    }


}
