package cn.lucasma.activiti.delegate;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * @author 邓仁波
 * @date 2018-7-30
 */
@Slf4j
public class MDCErrorDelegage implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        log.info("run MDCErrorDelegage");
        throw new RuntimeException("only test");
    }
}
