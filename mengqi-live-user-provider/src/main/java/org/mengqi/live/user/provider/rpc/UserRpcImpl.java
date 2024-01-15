package org.mengqi.live.user.provider.rpc;

import org.apache.dubbo.config.annotation.DubboService;
import org.mengqi.live.user.dto.UserDTO;
import org.mengqi.live.user.interfaces.IUserRpc;
import org.mengqi.live.user.provider.service.IUserService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@DubboService
public class UserRpcImpl implements IUserRpc {

    @Resource
    private IUserService userService;
    @Override
    public UserDTO getByUserId(Long userId) {
        return userService.getByUserId(userId);
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        boolean b = userService.updateUserInfo(userDTO);
        return b;
    }

    @Override
    public boolean insertOne(UserDTO userDTO) {
        return userService.insert(userDTO);
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        return userService.batchQueryUserInfo(userIdList);
    }
}
