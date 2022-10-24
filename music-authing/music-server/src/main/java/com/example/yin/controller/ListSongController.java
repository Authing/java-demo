package com.example.yin.controller;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import com.example.yin.common.ErrorMessage;
import com.example.yin.common.SuccessMessage;
import com.example.yin.domain.ListSong;
import com.example.yin.service.impl.ListSongServiceImpl;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class ListSongController {

    @Autowired
    private ListSongServiceImpl listSongService;

    @Autowired
    private AuthenticationClient authenticationClient;

    @Autowired
    private ManagementClient managementClient;

    private static final List<String> VIP_LIST;

    static {
        VIP_LIST = new ArrayList<>();
        VIP_LIST.addAll(Arrays.asList("1", "3", "5", "7", "9"));
    }

    // 给歌单添加歌曲
    @ResponseBody
    @RequestMapping(value = "/listSong/add", method = RequestMethod.POST)
    public Object addListSong(HttpServletRequest req) {
        String song_id = req.getParameter("songId").trim();
        String song_list_id = req.getParameter("songListId").trim();

        ListSong listsong = new ListSong();
        listsong.setSongId(Integer.parseInt(song_id));
        listsong.setSongListId(Integer.parseInt(song_list_id));

        boolean res = listSongService.addListSong(listsong);
        if (res) {
            return new SuccessMessage<Null>("添加成功").getMessage();
        } else {
            return new ErrorMessage("添加失败").getMessage();
        }
    }

    // 删除歌单里的歌曲
    @RequestMapping(value = "/listSong/delete", method = RequestMethod.GET)
    public Object deleteListSong(HttpServletRequest req) {
        String songId = req.getParameter("songId");

        boolean res = listSongService.deleteListSong(Integer.parseInt(songId));
        if (res) {
            return new SuccessMessage<Null>("删除成功").getMessage();
        } else {
            return new ErrorMessage("删除失败").getMessage();
        }
    }

    // 返回歌单里指定歌单 ID 的歌曲
    @RequestMapping(value = "/listSong/detail", method = RequestMethod.GET)
    public Object listSongOfSongId(HttpServletRequest req, HttpSession session) {
        String songListId = req.getParameter("songListId");

        // 获取歌单的 ids
        List<String> resources = getResources(session);
        if (resources.contains("*") || resources.contains(songListId)) {
            return new SuccessMessage<List<ListSong>>("添加成功", listSongService.listSongOfSongId(Integer.parseInt(songListId)))
                    .getMessage();
        }
        return new ErrorMessage("抱歉, 非 VIP 用户无法享用 VIP 歌单~").getMessage();
    }

    /**
     * 获取资源列表
     * @param session
     * @return
     */
    private List<String> getResources(HttpSession session) {
        GetUserRolesDto getUserRolesDto = new GetUserRolesDto();
        getUserRolesDto.setNamespace(authenticationClient.getOptions().getAppId());
        getUserRolesDto.setUserId(session.getAttribute("username").toString());
        getUserRolesDto.setUserIdType(ResignUserReqDto.UserIdType.USERNAME.getValue());
        //  调用 authing 接口 (获取用户的角色)
        RolePaginatedRespDto userRoles = managementClient.getUserRoles(getUserRolesDto);
        List<RoleDto> roles = userRoles.getData().getList();
        List<String> resources = new ArrayList<>();
        roles.forEach((dto) -> {
            GetRoleAuthorizedResourcesDto resourcesDto = new GetRoleAuthorizedResourcesDto();
            resourcesDto.setNamespace(authenticationClient.getOptions().getAppId());
            resourcesDto.setCode(dto.getCode());
            resourcesDto.setResourceType(ResourceItemDto.ResourceType.DATA.getValue());
            //  调用 authing 接口 (获取角色的资源)
            RoleAuthorizedResourcePaginatedRespDto roleAuthorizedResources = managementClient.getRoleAuthorizedResources(resourcesDto);
            List<RoleAuthorizedResourcesRespDto> list = roleAuthorizedResources.getData().getList();
            List<String> resource = new ArrayList<>();
            list.stream().forEach(item -> {
                String[] ids = item.getResourceCode().split(":")[1].split("_");
                resource.addAll(Arrays.asList(ids));
            });
            resources.addAll(resource);
        });
        return resources;
    }

    // 更新歌单里面的歌曲信息
    @ResponseBody
    @RequestMapping(value = "/listSong/update", method = RequestMethod.POST)
    public Object updateListSongMsg(HttpServletRequest req) {
        String id = req.getParameter("id").trim();
        String song_id = req.getParameter("songId").trim();
        String song_list_id = req.getParameter("songListId").trim();

        ListSong listsong = new ListSong();
        listsong.setId(Integer.parseInt(id));
        listsong.setSongId(Integer.parseInt(song_id));
        listsong.setSongListId(Integer.parseInt(song_list_id));

        boolean res = listSongService.updateListSongMsg(listsong);
        if (res) {
            return new SuccessMessage<Null>("修改成功").getMessage();
        } else {
            return new ErrorMessage("修改失败").getMessage();
        }
    }
}
