package org.mengqi.live.user.provider.service;


import org.mengqi.live.user.dto.UserDTO;

import java.util.List;
import java.util.Map;

public interface IUserService {


    UserDTO getByUserId(Long userId);

    boolean updateUserInfo(UserDTO userDTO);

    boolean insert(UserDTO userDTO);

    Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList);
}
