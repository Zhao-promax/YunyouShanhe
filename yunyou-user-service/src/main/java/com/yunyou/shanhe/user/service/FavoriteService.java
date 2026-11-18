package com.yunyou.shanhe.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.user.entity.UserFavorite;
import com.yunyou.shanhe.user.mapper.UserFavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户收藏服务。
 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserFavoriteMapper favoriteMapper;

    public void add(Long userId, Integer targetType, Long targetId) {
        Long count = favoriteMapper.selectCount(Wrappers.<UserFavorite>lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId));
        if (count != null && count > 0) {
            return; // 已收藏，幂等忽略（唯一索引兜底）
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setTargetType(targetType);
        favorite.setTargetId(targetId);
        favorite.setCreatedAt(System.currentTimeMillis());
        favoriteMapper.insert(favorite);
    }

    public List<UserFavorite> list(Long userId) {
        return favoriteMapper.selectList(Wrappers.<UserFavorite>lambdaQuery()
                .eq(UserFavorite::getUserId, userId)
                .orderByDesc(UserFavorite::getId));
    }

    public void remove(Long userId, Long id) {
        UserFavorite favorite = favoriteMapper.selectById(id);
        if (favorite == null || !favorite.getUserId().equals(userId)) {
            throw new BizException("收藏不存在");
        }
        favoriteMapper.deleteById(id);
    }
}
