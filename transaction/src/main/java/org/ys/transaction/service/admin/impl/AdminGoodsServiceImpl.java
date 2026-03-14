package org.ys.transaction.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.admin.AdminGoodsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台商品管理服务实现
 */
@Service
public class AdminGoodsServiceImpl implements AdminGoodsService {

    @Resource
    private YsGoodsDao ysGoodsDao;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public CommentResult getGoodsList(String keyword, String category, String status, Integer pageNum, Integer pageSize) {
        try {
            Page<YsGoods> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsGoods> wrapper = new QueryWrapper<>();

            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like("name", keyword)
                        .or()
                        .like("brand", keyword)
                        .or()
                        .like("introduce", keyword);
            }

            if (category != null && !category.isEmpty()) {
                wrapper.eq("category", category);
            }

            // 暂时不考虑商品状态字段，可以通过库存来判断是否下架
            if (status != null && !status.isEmpty()) {
                if ("1".equals(status)) {
                    wrapper.gt("inventory", 0);
                } else if ("0".equals(status)) {
                    wrapper.eq("inventory", 0);
                }
            }

            wrapper.orderByDesc("id");
            IPage<YsGoods> pageResult = ysGoodsDao.selectPage(page, wrapper);

            Map<String, Object> result = new HashMap<>();
            result.put("list", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            result.put("pageNum", pageResult.getCurrent());
            result.put("pageSize", pageResult.getSize());

            return CommentResult.success(result);
        } catch (Exception e) {
            return CommentResult.error("获取商品列表失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult addGoods(YsGoods goods) {
        try {
            int count = ysGoodsDao.insert(goods);
            if (count > 0) {
                return CommentResult.success("添加商品成功");
            }
            return CommentResult.error("添加商品失败");
        } catch (Exception e) {
            return CommentResult.error("添加商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult updateGoods(YsGoods goods) {
        try {
            int count = ysGoodsDao.updateById(goods);
            if (count > 0) {
                return CommentResult.success("更新商品成功");
            }
            return CommentResult.error("更新商品失败");
        } catch (Exception e) {
            return CommentResult.error("更新商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult deleteGoods(Long id) {
        try {
            int count = ysGoodsDao.deleteById(id);
            if (count > 0) {
                return CommentResult.success("删除商品成功");
            }
            return CommentResult.error("删除商品失败");
        } catch (Exception e) {
            return CommentResult.error("删除商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult batchDeleteGoods(List<Long> ids) {
        try {
            int count = ysGoodsDao.deleteBatchIds(ids);
            if (count > 0) {
                return CommentResult.success("批量删除商品成功，共删除 " + count + " 条");
            }
            return CommentResult.error("批量删除商品失败");
        } catch (Exception e) {
            return CommentResult.error("批量删除商品失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult updateGoodsStatus(Long id, Integer status) {
        try {
            YsGoods goods = ysGoodsDao.selectById(id);
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }

            UpdateWrapper<YsGoods> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", id);
            if (status == 1) {
                wrapper.set("inventory", goods.getInventory() > 0 ? goods.getInventory() : 100);
            } else {
                wrapper.set("inventory", 0);
            }

            int count = ysGoodsDao.update(null, wrapper);
            if (count > 0) {
                return CommentResult.success(status == 1 ? "商品上架成功" : "商品下架成功");
            }
            return CommentResult.error("更新商品状态失败");
        } catch (Exception e) {
            return CommentResult.error("更新商品状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult updateStock(Long goodsId, Integer stockChange) {
        try {
            YsGoods goods = ysGoodsDao.selectById(goodsId);
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }

            int newStock = goods.getInventory() + stockChange;
            if (newStock < 0) {
                return CommentResult.error("库存不能为负数");
            }

            UpdateWrapper<YsGoods> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", goodsId);
            wrapper.set("inventory", newStock);

            int count = ysGoodsDao.update(null, wrapper);
            if (count > 0) {
                return CommentResult.success("更新库存成功");
            }
            return CommentResult.error("更新库存失败");
        } catch (Exception e) {
            return CommentResult.error("更新库存失败：" + e.getMessage());
        }
    }

    @Override
    public CommentResult getGoodsDetail(Long id) {
        try {
            YsGoods goods = ysGoodsDao.selectById(id);
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }
            return CommentResult.success(goods);
        } catch (Exception e) {
            return CommentResult.error("获取商品详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommentResult batchImportGoods(List<YsGoods> goodsList) {
        try {
            int successCount = 0;
            for (YsGoods goods : goodsList) {
                int count = ysGoodsDao.insert(goods);
                if (count > 0) {
                    successCount++;
                }
            }
            return CommentResult.success("批量导入成功，共导入 " + successCount + " 条数据");
        } catch (Exception e) {
            return CommentResult.error("批量导入失败：" + e.getMessage());
        }
    }

    @Override
    public CommentResult exportGoods(String category) {
        try {
            QueryWrapper<YsGoods> wrapper = new QueryWrapper<>();
            if (category != null && !category.isEmpty()) {
                wrapper.eq("category", category);
            }
            List<YsGoods> list = ysGoodsDao.selectList(wrapper);
            return CommentResult.success(list);
        } catch (Exception e) {
            return CommentResult.error("导出商品失败：" + e.getMessage());
        }
    }
}
