package org.ys.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.commens.dao.YsGoodsDao;
import org.ys.commens.dao.YsProductFavoriteDao;
import org.ys.commens.entity.YsGoods;
import org.ys.commens.entity.YsProductFavorite;
import org.ys.commens.pojo.CommentResult;
import org.ys.transaction.service.FavoriteService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品收藏服务实现类
 *
 * @author makejava
 * @since 2025-07-16
 */
@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FavoriteServiceImpl.class);

    @Resource
    private YsProductFavoriteDao favoriteDao;

    @Resource
    private YsGoodsDao goodsDao;

    @Override
    public CommentResult addFavorite(Long userId, Long goodsId) {
        try {
            // 检查商品是否存在
            YsGoods goods = goodsDao.selectGoodById(goodsId);
            if (goods == null) {
                return CommentResult.error("商品不存在");
            }

            // 检查是否已收藏
            YsProductFavorite existingFavorite = favoriteDao.selectByUserAndGoods(userId, goodsId);
            if (existingFavorite != null) {
                return CommentResult.error("您已经收藏过该商品");
            }

            // 创建收藏记录
            YsProductFavorite favorite = new YsProductFavorite();
            favorite.setUserId(userId);
            favorite.setGoodsId(goodsId);
            favorite.setCreatedAt(new java.util.Date());

            int result = favoriteDao.insert(favorite);
            if (result > 0) {
                log.info("添加收藏成功: userId={}, goodsId={}", userId, goodsId);
                return CommentResult.success("收藏成功");
            } else {
                return CommentResult.error("收藏失败");
            }
        } catch (Exception e) {
            log.error("添加收藏失败: userId={}, goodsId={}, error={}", userId, goodsId, e.getMessage(), e);
            return CommentResult.error("收藏失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult removeFavorite(Long userId, Long goodsId) {
        try {
            // 查询收藏记录
            QueryWrapper<YsProductFavorite> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("goods_id", goodsId);
            YsProductFavorite favorite = favoriteDao.selectOne(queryWrapper);

            if (favorite == null) {
                return CommentResult.error("未找到收藏记录");
            }

            // 删除收藏
            int result = favoriteDao.deleteById(favorite.getId());
            if (result > 0) {
                log.info("取消收藏成功: userId={}, goodsId={}", userId, goodsId);
                return CommentResult.success("取消收藏成功");
            } else {
                return CommentResult.error("取消收藏失败");
            }
        } catch (Exception e) {
            log.error("取消收藏失败: userId={}, goodsId={}, error={}", userId, goodsId, e.getMessage(), e);
            return CommentResult.error("取消收藏失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult checkFavorite(Long userId, Long goodsId) {
        try {
            // 查询收藏记录
            YsProductFavorite favorite = favoriteDao.selectByUserAndGoods(userId, goodsId);

            Map<String, Object> result = new HashMap<>();
            result.put("isFavorited", favorite != null);

            return CommentResult.success(result);
        } catch (Exception e) {
            log.error("检查收藏状态失败: userId={}, goodsId={}, error={}", userId, goodsId, e.getMessage(), e);
            return CommentResult.error("检查失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult getUserFavorites(Long userId, Integer pageNum, Integer pageSize) {
        try {
            // 分页参数
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            Page<YsProductFavorite> page = new Page<>(pageNum, pageSize);
            QueryWrapper<YsProductFavorite> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            queryWrapper.orderByDesc("created_at");

            // 分页查询收藏列表
            IPage<YsProductFavorite> favoritePage = favoriteDao.selectPage(page, queryWrapper);

            // 获取商品详情
            List<YsProductFavorite> favorites = favoritePage.getRecords();
            for (YsProductFavorite favorite : favorites) {
                YsGoods goods = goodsDao.selectGoodById(favorite.getGoodsId());
                favorite.setGoods(goods);
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", favoritePage.getRecords());
            result.put("total", favoritePage.getTotal());
            result.put("pageNum", favoritePage.getCurrent());
            result.put("pageSize", favoritePage.getSize());
            result.put("pages", favoritePage.getPages());

            return CommentResult.success(result);
        } catch (Exception e) {
            log.error("获取用户收藏列表失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("获取收藏列表失败: " + e.getMessage());
        }
    }

    @Override
    public CommentResult batchRemoveFavorites(Long userId, String goodsIds) {
        try {
            if (goodsIds == null || goodsIds.trim().isEmpty()) {
                return CommentResult.error("请选择要取消收藏的商品");
            }

            String[] goodsIdArray = goodsIds.split(",");
            int successCount = 0;

            for (String goodsIdStr : goodsIdArray) {
                try {
                    Long goodsId = Long.parseLong(goodsIdStr.trim());
                    QueryWrapper<YsProductFavorite> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("user_id", userId).eq("goods_id", goodsId);
                    int result = favoriteDao.delete(queryWrapper);
                    if (result > 0) {
                        successCount++;
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的商品ID: {}", goodsIdStr);
                }
            }

            log.info("批量取消收藏成功: userId={}, successCount={}", userId, successCount);
            return CommentResult.success("成功取消" + successCount + "个收藏");
        } catch (Exception e) {
            log.error("批量取消收藏失败: userId={}, error={}", userId, e.getMessage(), e);
            return CommentResult.error("批量取消失败: " + e.getMessage());
        }
    }
}
