package com.yunyou.shanhe.content.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yunyou.shanhe.content.entity.City;
import com.yunyou.shanhe.content.mapper.CityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityMapper cityMapper;

    public List<City> list() {
        return cityMapper.selectList(Wrappers.<City>lambdaQuery()
                .eq(City::getStatus, 1)
                .orderByAsc(City::getSort));
    }
}
