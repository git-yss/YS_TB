package org.ys.transaction.Infrastructure.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.ys.transaction.Infrastructure.es.EsGoodsSearchService;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Canal binlog -> ES 增量同步（商品索引）
 *
 * 说明：
 * - 仅监听配置的 schema/table（默认 ys_tb.ys_goods）
 * - INSERT/UPDATE：按主键同步到 ES
 * - DELETE：按主键从 ES 删除
 */
@Component
@ConditionalOnProperty(prefix = "app.canal", name = "enabled", havingValue = "true")
public class CanalGoodsSyncRunner implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(CanalGoodsSyncRunner.class);

    @Value("${app.canal.host:127.0.0.1}")
    private String host;

    @Value("${app.canal.port:11111}")
    private Integer port;

    @Value("${app.canal.destination:example}")
    private String destination;

    @Value("${app.canal.username:}")
    private String username;

    @Value("${app.canal.password:}")
    private String password;

    @Value("${app.canal.batch-size:200}")
    private Integer batchSize;

    @Value("${app.canal.poll-interval-ms:1000}")
    private Long pollIntervalMs;

    @Value("${app.canal.schema:ys_tb}")
    private String schema;

    @Value("${app.canal.goods-table:ys_goods}")
    private String goodsTable;

    @Resource
    private EsGoodsSearchService esGoodsSearchService;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread worker;
    private CanalConnector connector;

    @Override
    public synchronized void start() {
        if (running.get()) return;
        running.set(true);

        connector = CanalConnectors.newSingleConnector(
                new InetSocketAddress(host, port),
                destination,
                username == null ? "" : username,
                password == null ? "" : password
        );

        worker = new Thread(this::consumeLoop, "canal-goods-sync-worker");
        worker.setDaemon(true);
        worker.start();
        log.info("Canal goods sync started. destination={}, target={}.{}", destination, schema, goodsTable);
    }

    @Override
    public synchronized void stop() {
        running.set(false);
        if (worker != null) {
            worker.interrupt();
        }
        if (connector != null) {
            try {
                connector.disconnect();
            } catch (Exception ignored) {
            }
        }
        log.info("Canal goods sync stopped.");
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(@NonNull Runnable callback) {
        stop();
        callback.run();
    }

    private void consumeLoop() {
        try {
            connector.connect();
            connector.subscribe(schema + "\\." + goodsTable);
            connector.rollback();

            while (running.get()) {
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                int size = message.getEntries() == null ? 0 : message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    sleepQuietly(pollIntervalMs);
                    continue;
                }
                try {
                    handleEntries(message.getEntries());
                    connector.ack(batchId);
                } catch (Exception e) {
                    log.error("Canal batch handle failed, rollback batchId={}", batchId, e);
                    connector.rollback(batchId);
                    sleepQuietly(Math.max(1000L, pollIntervalMs));
                }
            }
        } catch (Exception e) {
            log.error("Canal consume loop stopped unexpectedly", e);
        } finally {
            try {
                connector.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    private void handleEntries(List<CanalEntry.Entry> entries) throws Exception {
        for (CanalEntry.Entry entry : entries) {
            if (entry.getEntryType() != CanalEntry.EntryType.ROWDATA) continue;
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            CanalEntry.EventType eventType = rowChange.getEventType();

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                Long goodsId = extractGoodsId(rowData, eventType);
                if (goodsId == null || goodsId <= 0) continue;

                if (eventType == CanalEntry.EventType.DELETE) {
                    esGoodsSearchService.deleteGoodsById(goodsId);
                } else if (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE) {
                    esGoodsSearchService.syncGoodsById(goodsId);
                }
            }
        }
    }

    private Long extractGoodsId(CanalEntry.RowData rowData, CanalEntry.EventType eventType) {
        List<CanalEntry.Column> cols =
                eventType == CanalEntry.EventType.DELETE ? rowData.getBeforeColumnsList() : rowData.getAfterColumnsList();
        for (CanalEntry.Column col : cols) {
            if ("id".equalsIgnoreCase(col.getName())) {
                try {
                    return Long.valueOf(col.getValue());
                } catch (Exception ignore) {
                    return null;
                }
            }
        }
        return null;
    }

    private static void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
