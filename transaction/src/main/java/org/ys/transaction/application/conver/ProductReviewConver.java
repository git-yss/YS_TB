package org.ys.transaction.application.conver;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.ys.transaction.Interface.VO.ProductReviewVO;

@Mapper
public interface ProductReviewConver {
    ProductReviewConver INSTANCE = Mappers.getMapper(ProductReviewConver.class);

    default ProductReviewVO toReviewVO(org.ys.transaction.domain.entity.YsProductReview review) {
        if (review == null) {
            return null;
        }
        ProductReviewVO vo = new ProductReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setGoodsId(review.getGoodsId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setImages(review.getImages());
        vo.setIsAnonymous(review.getIsAnonymous());
        vo.setCreatedAt(review.getCreatedAt());
        vo.setUpdatedAt(review.getUpdatedAt());
        vo.setReplyContent(review.getReplyContent());
        vo.setReplyTime(review.getReplyTime());

        boolean anonymous = review.getIsAnonymous() != null && review.getIsAnonymous() == 1;
        vo.setUserId(anonymous ? null : review.getUserId());
        vo.setUsername(anonymous ? "匿名用户" : review.getUsername());
        return vo;
    }
}
