package com.example.yin2.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.dto.authentication.UserInfo;
import cn.hutool.core.util.StrUtil;
import com.example.yin2.common.ErrorMessage;
import com.example.yin2.common.SuccessMessage;
import com.example.yin2.domain.Collect;
import com.example.yin2.service.impl.CollectServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;

@RestController
public class CollectController {

    @Autowired
    private CollectServiceImpl collectService;

    @Autowired
    private AuthenticationClient authenticationClient;

    // 添加收藏的歌曲
    @ResponseBody
    @RequestMapping(value = "/collection/add", method = RequestMethod.POST)
    public Object addCollection(HttpServletRequest req,@CookieValue(value = "userAccessToken",required = false) String accessToken) {
        if(StrUtil.isBlank(accessToken)){
            return new ErrorMessage("accessToken 已失效，请重新登录").getMessage();
        }
//        String user_id = req.getParameter("userId");
        String type = req.getParameter("type");
        String song_id = req.getParameter("songId");
        String song_list_id = req.getParameter("songListId");

        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String authingUserId = userInfo.getSub();

        Collect collect = new Collect();
//        collect.setUserId(Integer.parseInt(user_id));
        collect.setType(new Byte(type));

        collect.setOwnerId(authingUserId);

        if (new Byte(type) == 0) {
            collect.setSongId(Integer.parseInt(song_id));
        } else if (new Byte(type) == 1) {
            collect.setSongListId(Integer.parseInt(song_list_id));
        }
        collect.setCreateTime(new Date());

        boolean res = collectService.addCollection(collect);
        if (res) {
            return new SuccessMessage<Boolean>("收藏成功", true).getMessage();
        } else {
            return new ErrorMessage("收藏失败").getMessage();
        }
    }

    // 取消收藏的歌曲
    @RequestMapping(value = "/collection/delete", method = RequestMethod.DELETE)
    public Object deleteCollection(HttpServletRequest req,@CookieValue(value = "userAccessToken",required = false) String accessToken) {
        if(StrUtil.isBlank(accessToken)){
            return new ErrorMessage("accessToken 已失效，请重新登录").getMessage();
        }
//        String user_id = req.getParameter("userId").trim();
        String song_id = req.getParameter("songId").trim();

        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String authingUserId = userInfo.getSub();

        boolean res = collectService.deleteCollect(authingUserId, Integer.parseInt(song_id));
        if (res) {
            return new SuccessMessage<Boolean>("取消收藏", false).getMessage();
        } else {
            return new ErrorMessage("取消收藏失败").getMessage();
        }
    }

    // 是否收藏歌曲
    @RequestMapping(value = "/collection/status", method = RequestMethod.POST)
    public Object isCollection(HttpServletRequest req, @CookieValue(value = "userAccessToken",required = false) String accessToken) {
        if(StrUtil.isBlank(accessToken)){
            return new ErrorMessage("accessToken 已失效，请重新登录").getMessage();
        }
        //        String user_id = req.getParameter("userId").trim();
        String song_id = req.getParameter("songId").trim();

        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String authingUserId = userInfo.getSub();

        boolean res = collectService.existSongId(authingUserId, Integer.parseInt(song_id));
        if (res) {
            return new SuccessMessage<Boolean>("已收藏", true).getMessage();
        } else {
            return new SuccessMessage<Boolean>("未收藏", false).getMessage();
        }
    }

    // 返回的指定用户 ID 收藏的列表
    @RequestMapping(value = "/collection/detail", method = RequestMethod.GET)
    public Object collectionOfUser(HttpServletRequest req,@CookieValue(value = "userAccessToken",required = false) String accessToken) {
        if(StrUtil.isBlank(accessToken)){
            return new ErrorMessage("accessToken 已失效，请重新登录").getMessage();
        }
//        String userId = req.getParameter("userId");
        UserInfo userInfo = authenticationClient.getUserInfoByAccessToken(accessToken);
        String authingUserId = userInfo.getSub();

        return new SuccessMessage<List<Collect>>("取消收藏", collectService.collectionOfUser(authingUserId)).getMessage();
    }
}
