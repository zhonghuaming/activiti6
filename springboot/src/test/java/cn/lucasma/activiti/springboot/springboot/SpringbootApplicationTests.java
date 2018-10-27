package cn.lucasma.activiti.springboot.springboot;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SpringbootApplicationTests {
    @Autowired
private RuntimeService runtimeService;
    @Test
    public void contextLoads() {
        final ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
        log.info(processInstance.toString());
    }

}
