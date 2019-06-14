import com.el.core.impl.LockBoundedBufferContainer;
import com.el.core.impl.WaitNotifyBoundedBufferContainer;
import com.el.core.model.BasicBoundedBufferContainerService;
import com.el.entity.ExecuteJob;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author eddie
 * @createTime 2019-05-27
 * @description
 */
@Slf4j
public class Bootstrap {


    public static void main(String[] args) {
        lockContainerStarter();
    }

    private static void waitNotifyContainerStarter(){
        BasicBoundedBufferContainerService container = new WaitNotifyBoundedBufferContainer(10);
        start(container);
    }

    private static void lockContainerStarter(){
        BasicBoundedBufferContainerService container = new LockBoundedBufferContainer(10);
        start(container);
    }

    private static void start(BasicBoundedBufferContainerService container){
        container.setCustom(e -> {
            log.info("消费信息: {}", e);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        container.setProducer(() -> {
            try {
                ExecuteJob executeJob = new ExecuteJob("0", "2");
                log.info("生产消息:{}", executeJob);
                TimeUnit.SECONDS.sleep(1);
                return executeJob;
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return null;
        });

        container.start();

        log.info(container.getStatus().getDesc());

        try {
            TimeUnit.SECONDS.sleep(5);
            container.stop();
            log.info(container.getStatus().getDesc());
            TimeUnit.SECONDS.sleep(5);
            container.start();
            log.info(container.getStatus().getDesc());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
