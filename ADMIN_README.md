# 智能电商平台 - 后台管理系统

## 概述

后台管理系统为智能电商平台提供了完整的商品、订单、用户、营销等管理功能。

## 功能模块

### 1. 商品管理 (Product Management)

**接口前缀**: `/admin/goods`

#### 功能列表
- **商品列表查询** (`POST /list`): 支持关键词搜索、分类筛选、状态筛选、分页查询
- **添加商品** (`POST /add`): 添加新商品
- **更新商品** (`POST /update`): 更新商品信息
- **删除商品** (`POST /delete/{id}`): 删除单个商品
- **批量删除商品** (`POST /batchDelete`): 批量删除商品
- **商品上架** (`POST /shelve/{id}`): 商品上架
- **商品下架** (`POST /unshelve/{id}`): 商品下架
- **更新库存** (`POST /stock`): 更新商品库存
- **获取商品详情** (`GET /detail/{id}`): 获取商品详细信息
- **批量导入商品** (`POST /import`): 批量导入商品
- **导出商品** (`GET /export`): 导出商品数据

#### 商品状态说明
- 通过库存判断：库存 > 0 为上架状态，库存 = 0 为下架状态

### 2. 订单管理 (Order Management)

**接口前缀**: `/admin/order`

#### 功能列表
- **订单列表查询** (`POST /list`): 支持状态筛选、用户筛选、关键词搜索、分页查询
- **获取订单详情** (`GET /detail/{id}`): 获取订单详细信息
- **发货** (`POST /ship`): 订单发货，填写物流信息
- **批量发货** (`POST /batchShip`): 批量订单发货
- **确认退款** (`POST /refund`): 处理退款申请
- **取消订单** (`POST /cancel/{id}`): 取消订单
- **获取订单统计** (`GET /statistics`): 获取订单统计数据
- **导出订单** (`GET /export`): 导出订单数据
- **获取退款申请列表** (`POST /refundList`): 查询待处理的退款申请

#### 订单状态说明
- `0`: 待支付
- `1`: 已支付
- `2`: 已发货
- `3`: 已完成
- `4`: 退款中
- `5`: 已退款
- `6`: 已取消

### 3. 用户管理 (User Management)

**接口前缀**: `/admin/user`

#### 功能列表
- **用户列表查询** (`POST /list`): 支持关键词搜索、状态筛选、分页查询
- **获取用户详情** (`GET /detail/{id}`): 获取用户详细信息
- **封禁用户** (`POST /ban/{id}`): 封禁用户
- **解封用户** (`POST /unban/{id}`): 解封用户
- **更新用户余额** (`POST /balance`): 增加或扣除用户余额
- **获取用户订单列表** (`POST /{id}/orders`): 查询指定用户的订单
- **获取用户统计** (`GET /statistics`): 获取用户统计数据
- **批量封禁用户** (`POST /batchBan`): 批量封禁用户
- **批量解封用户** (`POST /batchUnban`): 批量解封用户

#### 用户状态说明
- `null` 或 `1`: 正常
- `0`: 封禁

### 4. 营销管理 (Marketing Management)

**接口前缀**: `/admin/marketing`

#### 优惠券管理
- **获取优惠券列表** (`POST /coupon/list`): 查询优惠券列表
- **创建优惠券** (`POST /coupon/create`): 创建新优惠券
- **更新优惠券** (`POST /coupon/update`): 更新优惠券信息
- **删除优惠券** (`POST /coupon/delete/{id}`): 删除优惠券
- **发放优惠券** (`POST /coupon/distribute`): 向用户发放优惠券
- **获取优惠券统计** (`GET /coupon/statistics`): 获取优惠券使用统计

#### 秒杀管理
- **获取秒杀活动列表** (`POST /seckill/list`): 查询秒杀活动列表
- **创建秒杀活动** (`POST /seckill/create`): 创建新秒杀活动
- **更新秒杀活动** (`POST /seckill/update`): 更新秒杀活动信息
- **删除秒杀活动** (`POST /seckill/delete/{id}`): 删除秒杀活动
- **启用秒杀活动** (`POST /seckill/enable/{id}`): 启用秒杀活动
- **禁用秒杀活动** (`POST /seckill/disable/{id}`): 禁用秒杀活动
- **获取秒杀统计** (`GET /seckill/statistics`): 获取秒杀活动统计

### 5. 数据统计 (Statistics)

**接口前缀**: `/admin`

#### 功能列表
- **获取后台首页统计** (`GET /dashboard`): 获取关键业务指标
  - 总订单数
  - 已支付订单数
  - 待处理订单数
  - 已发货订单数
  - 总用户数
  - 总商品数
  - 总销售额
  - 今日新增订单
  - 今日新增用户

### 6. 系统管理 (System Management)

**接口前缀**: `/admin`

#### 功能列表
- **后台登录** (`POST /login`): 管理员登录
  - 默认账号: `admin`
  - 默认密码: `admin123`
- **获取系统配置** (`GET /config`): 获取系统配置信息
- **更新系统配置** (`POST /config`): 更新系统配置

## 数据库设计

### 核心表

#### 1. 管理员表 (`ys_admin`)
- 管理员信息存储
- 角色权限管理
- 登录状态追踪

#### 2. 商品分类表 (`ys_category`)
- 支持多级分类
- 分类图标和描述

#### 3. 优惠券表 (`ys_coupon`)
- 优惠券类型：满减券、折扣券、无门槛券
- 优惠券使用规则和限制

#### 4. 用户优惠券表 (`ys_user_coupon`)
- 用户领取的优惠券
- 使用状态和过期时间

#### 5. 秒杀活动表 (`ys_seckill`)
- 秒杀商品和价格
- 活动时间范围

#### 6. 秒杀订单表 (`ys_seckill_order`)
- 秒杀订单记录
- 支付状态和时间

#### 7. 商品评价表 (`ys_product_review`)
- 用户商品评价
- 评分和回复

#### 8. 商品收藏表 (`ys_product_favorite`)
- 用户收藏商品记录

#### 9. 系统配置表 (`ys_system_config`)
- 系统参数配置
- 业务开关设置

#### 10. 操作日志表 (`ys_admin_log`)
- 管理员操作记录
- 审计追踪

## 部署说明

### 1. 数据库初始化

执行数据库脚本：

```bash
mysql -u root -p < commens/src/main/resources/sqlScript/admin_tables.sql
```

### 2. 默认账号

系统会自动创建一个超级管理员账号：

- **账号**: admin
- **密码**: admin123
- **角色**: super_admin

⚠️ **重要**: 生产环境请务必修改默认密码！

### 3. 访问地址

- **后台管理系统API**: `http://localhost:8080/admin`
- **Swagger文档**: `http://localhost:8080/swagger-ui.html`

## 安全配置

### 1. 认证方式

当前使用Session-based认证，生产环境建议升级为JWT Token认证。

### 2. 权限控制

- 基于角色的访问控制 (RBAC)
- 支持细粒度的权限管理
- 操作日志记录

### 3. 拦截器

后台接口通过 `AdminInterceptor` 进行权限校验：
- 公开接口：`/admin/login`, `/admin/config`
- 需要登录的接口：其他所有 `/admin/**` 接口

## API使用示例

### 1. 管理员登录

```bash
curl -X POST http://localhost:8080/admin/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. 获取商品列表

```bash
curl -X POST http://localhost:8080/admin/goods/list \
  -H "Content-Type: application/json" \
  -d '{"pageNum":1,"pageSize":10,"keyword":"iPhone"}'
```

### 3. 发货

```bash
curl -X POST http://localhost:8080/admin/order/ship \
  -H "Content-Type: application/json" \
  -d '{"orderId":123456,"logisticsCompany":"顺丰快递","logisticsNo":"SF1234567890"}'
```

### 4. 获取统计数据

```bash
curl -X GET http://localhost:8080/admin/dashboard
```

## 扩展功能

### 已实现的模块

- ✅ 商品管理 (增删改查、上下架、库存管理)
- ✅ 订单管理 (查询、发货、退款)
- ✅ 用户管理 (查询、封禁、余额调整)
- ✅ 数据统计 (关键指标展示)
- ⚠️ 优惠券管理 (框架已完成，待完善业务逻辑)
- ⚠️ 秒杀管理 (框架已完成，待完善业务逻辑)

### 待实现功能

- ⏳ 权限管理界面
- ⏳ 操作日志查看
- ⏳ 数据导出优化
- ⏳ 批量操作优化
- ⏳ 消息通知
- ⏳ 前端管理界面

## 技术栈

- **后端框架**: Spring Boot 2.7.15
- **ORM框架**: MyBatis-Plus 3.4.2
- **数据库**: MySQL 8.0+
- **缓存**: Redis (Redisson 3.23.2)
- **消息队列**: ActiveMQ
- **安全框架**: Spring Security

## 项目结构

```
transaction/
├── src/main/java/org/ys/transaction/
│   ├── controller/admin/          # 后台管理控制器
│   │   ├── AdminController.java
│   │   ├── AdminGoodsController.java
│   │   ├── AdminOrderController.java
│   │   ├── AdminUserController.java
│   │   └── AdminMarketingController.java
│   ├── service/admin/             # 后台管理服务接口
│   │   ├── AdminGoodsService.java
│   │   ├── AdminOrderService.java
│   │   ├── AdminUserService.java
│   │   ├── AdminStatisticsService.java
│   │   ├── AdminCouponService.java
│   │   └── AdminSeckillService.java
│   ├── service/admin/impl/        # 后台管理服务实现
│   │   ├── AdminGoodsServiceImpl.java
│   │   ├── AdminOrderServiceImpl.java
│   │   ├── AdminUserServiceImpl.java
│   │   ├── AdminStatisticsServiceImpl.java
│   │   ├── AdminCouponServiceImpl.java
│   │   └── AdminSeckillServiceImpl.java
│   ├── interceptor/               # 拦截器
│   │   └── AdminInterceptor.java
│   └── config/                    # 配置类
│       ├── AdminSecurityConfig.java
│       └── InterceptorConfig.java
```

## 注意事项

1. **安全性**
   - 生产环境必须修改默认管理员密码
   - 建议使用HTTPS协议
   - 实施IP白名单限制

2. **性能优化**
   - 列表查询使用分页
   - 统计数据考虑使用缓存
   - 导出功能限制数据量

3. **数据备份**
   - 定期备份数据库
   - 重要操作前创建备份

4. **日志监控**
   - 关键操作记录日志
   - 异常及时告警
   - 定期审查操作日志

## 联系方式

如有问题或建议，请联系开发团队。

---

**版本**: 1.0.0
**最后更新**: 2026-03-14
