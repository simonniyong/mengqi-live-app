package org.mengqi.live.user.interfaces;


import org.mengqi.live.user.dto.UserDTO;
import java.util.List;
import java.util.Map;

public interface IUserRpc {
    UserDTO getByUserId(Long userId);

    boolean updateUserInfo(UserDTO userDTO);

    boolean insertOne(UserDTO userDTO);

    Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList);
}
