package com.yunyou.shanhe.user.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.common.core.BizException;
import com.yunyou.shanhe.user.dto.RealNameRequest;
import com.yunyou.shanhe.user.entity.RealNameProfile;
import com.yunyou.shanhe.user.mapper.RealNameProfileMapper;
import com.yunyou.shanhe.user.util.AesUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 出行人实名档案管理。
 */
@Service
@RequiredArgsConstructor
public class RealNameService {

    private final RealNameProfileMapper mapper;
    private final AesUtil aesUtil;

    public RealNameProfile add(Long userId, RealNameRequest request) {
        long now = System.currentTimeMillis();
        RealNameProfile profile = new RealNameProfile();
        profile.setUserId(userId);
        profile.setRealName(request.realName());
        profile.setIdCardNoEnc(aesUtil.encrypt(request.idCardNo()));
        profile.setIdCardType(1);
        profile.setIdCardMask(mask(request.idCardNo()));
        profile.setStatus(1);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        mapper.insert(profile);
        return profile;
    }

    public List<RealNameProfile> list(Long userId) {
        return mapper.selectList(Wrappers.<RealNameProfile>lambdaQuery()
                .eq(RealNameProfile::getUserId, userId)
                .eq(RealNameProfile::getStatus, 1));
    }

    public void delete(Long userId, Long id) {
        RealNameProfile profile = mapper.selectById(id);
        if (profile == null || !profile.getUserId().equals(userId)) {
            throw new BizException("实名信息不存在");
        }
        mapper.deleteById(id);
    }

    /**
     * 归档超期未使用的实名档案（软删除）。合规要求：游玩核销+退票窗口期后按协议归档。
     */
    public int archiveExpired(int days) {
        long threshold = System.currentTimeMillis() - days * 24L * 3600 * 1000;
        List<RealNameProfile> expired = mapper.selectList(Wrappers.<RealNameProfile>lambdaQuery()
                .eq(RealNameProfile::getStatus, 1)
                .le(RealNameProfile::getUpdatedAt, threshold));
        int archived = 0;
        for (RealNameProfile p : expired) {
            RealNameProfile update = new RealNameProfile();
            update.setId(p.getId());
            update.setStatus(0);
            update.setArchivedAt(System.currentTimeMillis());
            mapper.updateById(update);
            archived++;
        }
        return archived;
    }

    private String mask(String idCardNo) {
        if (idCardNo == null || idCardNo.length() < 8) {
            return idCardNo;
        }
        return idCardNo.substring(0, 4) + "**********" + idCardNo.substring(idCardNo.length() - 4);
    }
}
