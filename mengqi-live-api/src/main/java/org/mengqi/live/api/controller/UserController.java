package org.mengqi.live.api.controller;

import org.apache.dubbo.config.annotation.DubboReference;
import org.mengqi.live.user.dto.UserDTO;
import org.mengqi.live.user.interfaces.IUserRpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @DubboReference
    private IUserRpc iUserRpc;

    @GetMapping("/getUserInfo")
    public UserDTO dubbo(Long userId){
        return iUserRpc.getByUserId(userId);
    }

    @GetMapping("/updateUserInfo")
    public boolean getUserInfo(Long userId,String nickname){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName(nickname);
        return iUserRpc.updateUserInfo(userDTO);
    }

    @GetMapping("/insertOne")
    public boolean insertOne(Long userId){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setNickName("idea-test");
        userDTO.setSex(1);
        return iUserRpc.insertOne(userDTO);
    }

    @GetMapping("/batchQueryUser")
    public Map<Long,UserDTO> batchQueryUserInfo(List<Long> userIdList){
        return iUserRpc.batchQueryUserInfo(userIdList);
    }
}
