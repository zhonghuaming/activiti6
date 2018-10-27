package cn.lucasma.activiti.coreapi;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 流程储存服务
 * 管理流程定义文件xml以及静态资源文件
 * 对特定流程的暂停和激活
 * 流程定义启动权限管理
 * <p>
 * 涉及api：
 * DeploymentBuilder 部署文件构造器
 * DeploymentQuery  部署文件查询器
 * ProcessDefinitionQuery 流程定义文件查询对象
 * Deployment 流程部署文件对象
 * ProcessDefinition 流程定义文件对象
 * BpmnModel 流程定义的java格式
 */
public class RepositoryServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryServiceTest.class);

    /**
     * 使用默认的流程配置文件
     */
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    /**
     * 流程文件定义部署demo
     */
    @Test
    public void testRepostory() {
        /**
         * 负责对流程静态文件的管理, 如流程 xml 流程图片
         * 部署对象， 流程定义对象
         * 部署对象和流程定义对象是 一对多的关系
         */
        RepositoryService repositoryService = activitiRule.getRepositoryService();
        // 流程部署对象
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.name("测试部署资源1")
                .addClasspathResource("my-process.bpmn20.xml")
                .addClasspathResource("second_approve.bpmn20.xml");

        //部署
        Deployment deploy = deploymentBuilder.deploy();
        logger.info("deploy ={}", deploy);

        DeploymentBuilder deploymentBuilder2 = repositoryService.createDeployment();
        deploymentBuilder2.name("测试部署资源2")
                .addClasspathResource("my-process.bpmn20.xml")
                .addClasspathResource("second_approve.bpmn20.xml");
        Deployment deploy1 = deploymentBuilder2.deploy();

        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
        List<Deployment> deploymentList = deploymentQuery
//                .deploymentId(deploy.getId()).singleResult();
                .orderByDeploymenTime().asc().listPage(0, 100);

        for (Deployment deployment : deploymentList) {
            logger.info("deployment = {}", deployment);
        }
        logger.info("deploymentList size = {}", deploymentList.size());
//        ProcessDefinition流程定义对象 流程定义对象多次部署每次版本号加+1
        List<ProcessDefinition> definitionList = repositoryService
                .createProcessDefinitionQuery()
//                .deploymentId(deploy.getId())
                .orderByProcessDefinitionKey().asc()
                .listPage(0, 100);
        //        ProcessDefinition流程定义对象id 为 key:版本:数据库自增id
        for (ProcessDefinition processDefinition : definitionList) {
            logger.info("processDefinition = {},version = {},key ={},id = {}",
                    processDefinition,
                    processDefinition.getVersion(),
                    processDefinition.getKey(),
                    processDefinition.getId());
        }
    }


    /**
     * 流程定义文件挂起 激活demo
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testSuspend() {
        RepositoryService repositoryService = activitiRule.getRepositoryService();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
        //流程定义文件id
        logger.info("processDefinition.id = {}", processDefinition.getId());

        //挂起流程定义对象
        repositoryService.suspendProcessDefinitionById(processDefinition.getId());
        try {
            logger.info("开始启动");
            //尝试启动流程定义对象
            activitiRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
            logger.info("启动成功");
        } catch (Exception e) {
            logger.info("启动失败");
            logger.info(e.getMessage(), e);
        }

        //激活流程定义对象
        repositoryService.activateProcessDefinitionById(processDefinition.getId());

        logger.info("开始启动");
        activitiRule.getRuntimeService().startProcessInstanceById(processDefinition.getId());
        logger.info("启动成功");

    }

    /**
     * 为流程定义绑定关系 指定用户或者用户组
     */
    @Test
    @org.activiti.engine.test.Deployment(resources = {"my-process.bpmn20.xml"})
    public void testCandidateStarter() {
        RepositoryService repositoryService = activitiRule.getRepositoryService();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        logger.info("processDefinition.id = {}", processDefinition.getId());

        //创建关系
        // 添加一个指定的用户 user
        repositoryService.addCandidateStarterUser(processDefinition.getId(), "user");
        // 添加一个用户组 用户组名称 groupM
        repositoryService.addCandidateStarterGroup(processDefinition.getId(), "groupM");

        //根据流程定义获取关系
        List<IdentityLink> identityLinkList = repositoryService.getIdentityLinksForProcessDefinition(
                processDefinition.getId());

        for (IdentityLink identityLink : identityLinkList) {
            logger.info("identityLink =  {}", identityLink);
        }

        //删除关系
        repositoryService.deleteCandidateStarterGroup(processDefinition.getId(), "groupM");
        repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "user");


    }

}
