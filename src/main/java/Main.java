import com.el.core.impl.WaitNotifyBoundedBufferContainer;
import com.el.entity.ExecuteJob;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author eddie
 * @createTime 2019-05-27
 * @description
 */
@Slf4j
public class Main {

    private static WaitNotifyBoundedBufferContainer container = new WaitNotifyBoundedBufferContainer(4);

    public static void main(String[] args) {
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
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            return new ExecuteJob("0", "2");
        });
        container.start();

        log.info(container.getStatus().getDesc());
    }
}
