package org.mengqi.live.user.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    private Long userId;
    private String nickName;
    private String trueName;
    private String avatar;
    private Integer sex;
    private Integer workCity;
    private Integer bornCity;
    private Date createTime;
    private Date updateTime;
}
