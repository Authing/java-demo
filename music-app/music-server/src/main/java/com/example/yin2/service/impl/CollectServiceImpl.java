package com.example.yin2.service.impl;

import com.example.yin2.dao.CollectMapper;
import com.example.yin2.domain.Collect;
import com.example.yin2.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectServiceImpl implements CollectService {
    @Autowired
    private CollectMapper collectMapper;

    @Override
    public boolean addCollection(Collect collect) {
        return collectMapper.insertSelective(collect) > 0 ? true : false;
    }

    @Override
    public boolean existSongId(String authingUserId, Integer songId) {
        return collectMapper.existSongId(authingUserId, songId) > 0 ? true : false;
    }

    @Override
    public boolean deleteCollect(String authingUserId, Integer songId) {
        return collectMapper.deleteCollect(authingUserId, songId) > 0 ? true : false;
    }

    @Override
    public List<Collect> collectionOfUser(String authingUserId)

    {
        return collectMapper.collectionOfUser(authingUserId);
    }
}
