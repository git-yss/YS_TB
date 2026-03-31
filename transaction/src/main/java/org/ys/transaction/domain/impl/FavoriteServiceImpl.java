package org.ys.transaction.domain.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ys.transaction.domain.inteface.FavoriteService;
import org.ys.transaction.Infrastructure.dao.YsGoodsDao;
import org.ys.transaction.Infrastructure.dao.YsProductFavoriteDao;
import org.ys.transaction.Infrastructure.pojo.YsGoods;
import org.ys.transaction.Infrastructure.pojo.YsProductFavorite;

import javax.annotation.Resource;
import java.util.HashMap;
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
    public void addFavorite(Long userId, Long goodsId) {
        YsGoods goods = goodsDao.selectGoodById(goodsId);
        if (goods == null) {
            throw new IllegalStateException("商品不存在");
        }

        YsProductFavorite existingFavorite = favoriteDao.selectByUserAndGoods(userId, goodsId);
        if (existingFavorite != null) {
            throw new IllegalStateException("您已经收藏过该商品");
        }

        YsProductFavorite favorite = new YsProductFavorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreatedAt(new java.util.Date());

        int result = favoriteDao.insert(favorite);
        if (result <= 0) {
            throw new IllegalStateException("收藏失败");
        }
        log.info("添加收藏成功: userId={}, goodsId={}", userId, goodsId);
    }

    @Override
    public void removeFavorite(Long userId, Long goodsId) {
        QueryWrapper<YsProductFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("goods_id", goodsId);
        YsProductFavorite favorite = favoriteDao.selectOne(queryWrapper);

        if (favorite == null) {
            throw new IllegalStateException("未找到收藏记录");
        }

        int result = favoriteDao.deleteById(favorite.getId());
        if (result <= 0) {
            throw new IllegalStateException("取消收藏失败");
        }
        log.info("取消收藏成功: userId={}, goodsId={}", userId, goodsId);
    }

    @Override
    public Map<String, Object> checkFavorite(Long userId, Long goodsId) {
        YsProductFavorite favorite = favoriteDao.selectByUserAndGoods(userId, goodsId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorited", favorite != null);
        return result;
    }

    @Override
    public Map<String, Object> getUserFavorites(Long userId, Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;

        Page<YsProductFavorite> page = new Page<>(pageNum, pageSize);
        QueryWrapper<YsProductFavorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("created_at");

        IPage<YsProductFavorite> favoritePage = favoriteDao.selectPage(page, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", favoritePage.getRecords());
        result.put("total", favoritePage.getTotal());
        result.put("pageNum", favoritePage.getCurrent());
        result.put("pageSize", favoritePage.getSize());
        result.put("pages", favoritePage.getPages());
        return result;
    }

    @Override
    public String batchRemoveFavorites(Long userId, String goodsIds) {
        if (goodsIds == null || goodsIds.trim().isEmpty()) {
            throw new IllegalArgumentException("请选择要取消收藏的商品");
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
        return "成功取消" + successCount + "个收藏";
    }
}
