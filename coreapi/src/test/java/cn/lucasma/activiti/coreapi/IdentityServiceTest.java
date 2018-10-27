package cn.lucasma.activiti.coreapi;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 身份管理服务
 * 管理用户（User）
 * 管理用户组（Group）
 * 用户和用户组的关系(MemBerShip)
 *
 *
 */
public class IdentityServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(IdentityServiceTest.class);
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    public void testIdentity() {
        IdentityService identityService = activitiRule.getIdentityService();
        User user1 = identityService.newUser("user1");
        user1.setEmail("user1@126.com");
        User user2 = identityService.newUser("user2");
        user2.setEmail("user2@126.com");
        identityService.saveUser(user1);
        identityService.saveUser(user2);

        Group group1 = identityService.newGroup("group1");
        group1.setName("一组");
        identityService.saveGroup(group1);
        Group group2 = identityService.newGroup("group2");
        group2.setName("二组");
        identityService.saveGroup(group2);

        // 构建用户和组之间的关系
        identityService.createMembership("user1", "group1");
        identityService.createMembership("user2", "group1");
        identityService.createMembership("user1", "group2");


        User user11 = identityService.createUserQuery().userId("user1").singleResult();
        user11.setLastName("lucas");
        identityService.saveUser(user11);

        // 查询用户列表 根据组查询用户
        List<User> userList = identityService.createUserQuery().memberOfGroup("group1").listPage(0, 100);
        for (User user : userList) {
            logger.info("user = {}", ToStringBuilder.reflectionToString(user, ToStringStyle.JSON_STYLE));
        }

        // 查询 组 根据用户查询所属组
        List<Group> groupList = identityService.createGroupQuery().groupMember("user1").listPage(0, 100);

        for (Group group : groupList) {
            logger.info("group = {}", ToStringBuilder.reflectionToString(group, ToStringStyle.JSON_STYLE));
        }
    }
}
