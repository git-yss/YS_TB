# YS_TB

## 模拟电商平台

## Elasticsearch 企业级搜索实践

本项目已将商品搜索升级为 `MySQL + Elasticsearch` 双通路架构，目标是兼顾性能、相关性和可用性。

### 架构亮点

- 搜索接口无缝升级：前端仍调用 `POST /product/search`，后端优先走 ES
- 降级兜底：ES 异常时自动回退 MySQL 查询，保证业务连续性
- 全量重建：支持从数据库全量重建索引
- 增量同步：支持单商品更新后同步到 ES
- 联想建议：支持搜索下拉建议（completion suggest）
- 热词统计：支持搜索关键词热度统计（用于运营看板/推荐词）
- 别名切换：`alias -> version index`，支持零停机重建

### 代码主链路（你可以这样给面试官讲）

1. 请求进入 `POST /product/search`
2. `ProductApplicationService.searchProducts()` 优先调用 `EsGoodsSearchService.search()`
3. `EsGoodsSearchService.search()` 使用别名 `ys_goods_alias` 查询当前活跃索引
4. 若 ES 不可用，`ProductApplicationService` 自动降级到 MySQL 查询（兜底）

### 零停机重建链路（重点）

1. 调用 `POST /product/es/rebuildIndex`
2. 服务创建新版本索引：`ys_goods_index_v{timestamp}`
3. 从 MySQL 全量拉取商品并批量写入新索引
4. 调用 `/_aliases` 原子切换：
   - add：新索引绑定别名 `ys_goods_alias`
   - remove：旧索引移除别名
5. 线上搜索请求始终走别名，不需要停机

### ES 索引设计（核心字段）

- 别名：`ys_goods_alias`（对外查询/写入入口）
- 物理索引：`ys_goods_index_v*`（每次重建都会创建新版本索引，并用别名零停机切换）
- 核心 mapping：
  - `name` / `introduce`：`text`（全文检索）
  - `brand` / `category`：`text + keyword`（检索 + 精确筛选）
  - `categoryId`：`long`
  - `price`：`double`
  - `inventory`：`integer`
  - `suggest`：`completion`（联想词）

### 已支持的企业级检索能力

- 多字段相关性检索：`name` 权重最高，`brand` 次之，`introduce` 再次
- 过滤条件：分类、品牌、价格区间、库存下限
- 排序：价格升降序、库存升降序、最新上架
- 分页：`pageNum` + `pageSize`
- 高亮：关键词高亮回显（名称、介绍）

---

## ES 接口清单（面试可直接展示）

### 1) 商品搜索（主接口，ES优先）

- `POST /product/search`
- 请求示例：

```json
{
  "keyword": "手机",
  "categoryId": 1,
  "brand": "华为",
  "priceMin": 1000,
  "priceMax": 6000,
  "inventoryMin": 1,
  "sort": "priceAsc",
  "pageNum": 1,
  "pageSize": 20
}
```

### 2) 全量重建索引（初始化/回灌）

- `POST /product/es/rebuildIndex`
- 返回：`indexedCount`（成功写入ES的商品数量）

### 3) 单商品增量同步（商品变更后）

- `POST /product/es/syncByGoodsId`
- 请求示例：

```json
{
  "goodsId": 10001
}
```

### 4) 联想词建议（搜索框下拉）

- `GET /product/es/suggest?prefix=iph&size=10`
- 返回：联想词列表

### 5) 热门关键词统计（运营/推荐词）

- `GET /product/es/hotKeywords?size=10`
- 返回：`keyword + count`

### 6) 别名指向查询（零停机切换验证）

- `GET /product/es/aliasStatus`
- 返回：`alias` + `activeIndex`

---

## 本地启动与演示流程

1. 启动 Elasticsearch（默认 `localhost:9200`）
2. 启动后端服务（`transaction`）
3. 调用 `POST /product/es/rebuildIndex` 做一次全量建索引
   - 会创建新版本物理索引（`ys_goods_index_v*`）
   - 写入完成后通过 `/_aliases` 原子切换别名（零停机）
4. 用 `POST /product/search` 进行多条件搜索演示
5. 用 `GET /product/es/suggest` 演示联想词
6. 用 `GET /product/es/hotKeywords` 演示热词统计
7. 用 `GET /product/es/aliasStatus` 验证别名指向的物理索引

---

## 面试讲解话术建议（高频）

- **为什么要上 ES？**
  - MySQL 在复杂模糊检索、相关性排序和高并发搜索场景下成本高，ES 更适合搜索域。

- **如何保证稳定性？**
  - 采用 ES 优先 + DB 兜底，ES 故障不影响主流程可用性。

- **如何保证数据一致性？**
  - 提供全量重建 + 单条增量同步能力；可进一步升级为 MQ/CANAL 异步同步。

- **做了哪些“企业级”能力？**
  - 多字段加权检索、过滤/排序、分页、高亮、联想词、热词统计、可运维重建接口。

- **下一步可演进方向？**
  - 索引别名零停机切换、拼写纠错、同义词、分词器（IK）、召回+重排、A/B 搜索实验。

---

## ES 故障演练手册（可用于面试实操讲解）

本手册用于验证三类关键场景：
- ES 挂掉时系统是否可用（降级）
- 别名切换失败时如何定位
- 切换后结果异常时如何回滚

### 演练前准备

1. 确认当前别名指向：
   - `GET /product/es/aliasStatus`
2. 确认重建接口可用：
   - `POST /product/es/rebuildIndex`
3. 记录当前活跃索引（示例）：`ys_goods_index_v1775569529771`

---

### 场景一：ES 挂掉（验证降级兜底）

目标：验证 ES 不可用时，`/product/search` 不会把业务打挂。

步骤：

1. 停止 Elasticsearch 服务（本地服务/容器均可）
2. 调用搜索接口：
   - `POST /product/search`
3. 预期结果：
   - 接口仍返回 200（走 MySQL 降级）
   - 结果可能缺少 ES 特性（如高亮/联想）
4. 恢复 Elasticsearch 后再次调用：
   - 搜索自动恢复 ES 路径

排查点：
- 若返回 500，检查 `ProductApplicationService.searchProducts()` 的降级分支是否被改坏
- 检查 ES 异常是否在应用层被吞掉后正确 fallback

---

### 场景二：别名切换失败（重建过程中断）

目标：验证“新索引已建但别名未切换”时的定位能力。

典型现象：
- `POST /product/es/rebuildIndex` 返回失败
- `GET /product/es/aliasStatus` 显示仍指向旧索引

排查顺序（Kibana Dev Tools / curl）：

1. 查看别名状态：
   - `GET /_alias/ys_goods_alias`
2. 查看物理索引列表：
   - `GET /_cat/indices/ys_goods_index_v*?v`
3. 判断失败位置：
   - 若存在新索引但 alias 仍是旧索引，说明失败在别名切换阶段
   - 若新索引都不存在，说明失败在建索引/写入阶段

应急处理（手工原子切换）：

```json
POST /_aliases
{
  "actions": [
    { "add":    { "index": "ys_goods_index_vNEW", "alias": "ys_goods_alias" }},
    { "remove": { "index": "ys_goods_index_vOLD", "alias": "ys_goods_alias" }}
  ]
}
```

---

### 场景三：切换后查询异常（回滚）

目标：新索引切换后效果异常（召回下降/数据异常）时快速回滚。

前提：
- 保留上一个稳定索引（不要立即删除旧索引）
- 已记录旧索引名（例如 `ys_goods_index_vOLD`）

回滚操作（原子）：

```json
POST /_aliases
{
  "actions": [
    { "add":    { "index": "ys_goods_index_vOLD", "alias": "ys_goods_alias" }},
    { "remove": { "index": "ys_goods_index_vNEW", "alias": "ys_goods_alias" }}
  ]
}
```

回滚验证：
1. `GET /product/es/aliasStatus` 应指回旧索引
2. 执行核心搜索用例（关键词、品牌、价格区间）验证结果恢复
3. 记录故障原因（mapping、分词器、数据导入脚本、字段缺失等）

---

### 线上演练建议（最佳实践）

- 小流量先演练，避免高峰期直接切换
- 每次重建都保留最近 1~2 个历史索引，便于秒级回滚
- 演练输出固定模板：
  - 演练目标
  - 操作步骤
  - 观测指标（成功率、响应时间、命中率）
  - 回滚耗时
  - 复盘结论

---

## Canal 同步索引（Binlog -> ES）

已支持通过 Canal 监听 MySQL binlog 自动同步商品索引，替代“手工触发同步”。

### 同步策略

- 监听表：`ys_tb.ys_goods`
- `INSERT/UPDATE`：调用 ES 增量同步（按商品 ID 重建文档）
- `DELETE`：调用 ES 删除文档（按商品 ID）

### 关键代码

- `transaction/src/main/java/org/ys/transaction/Infrastructure/canal/CanalGoodsSyncRunner.java`
  - 使用 `CanalConnector` 拉取 binlog
  - 批量 ACK/失败回滚
  - 仅处理商品表数据变更

### 配置项（application.yml）

```yml
app:
  canal:
    enabled: false
    host: 127.0.0.1
    port: 11111
    destination: example
    username:
    password:
    batch-size: 200
    poll-interval-ms: 1000
    schema: ys_tb
    goods-table: ys_goods
```

### 启用步骤

1. 启动 MySQL binlog（ROW 模式）  
2. 启动 Canal Server（destination 与配置一致）  
3. 将 `app.canal.enabled` 设为 `true` 并重启后端  
4. 对 `ys_goods` 做增删改，观察 ES 索引变化  

### 未解决点：（提供解决思路待后续补充）

一、	秒杀接口异步触发的扣库存，存在如果支付时该接口还没扣除库存，支付接口需要手动补偿扣除库存需求，但是扣除发现库存不足，则业务逻辑上就导致抢到商品无法支付的情况;

解决方案：

1、降低秒杀接口性能，将扣库存数据库操作放在秒杀接口中保证同步一致性
2、业务上保证商品不会缺货，商品缺货时及时进货

二、可优化点：将秒杀订单的redis操作采用lua来原子性执行，可进一步提升性能；



功能点：点击促销，进入页面可以查询到所有待秒杀商品！可以进行秒杀！

点击我的个人中心可以修改密码账户，手机号 邮箱地址等信息！

点击购物车进入购物车页面，可以选中点击购买再弹到支付界面，再支付；





