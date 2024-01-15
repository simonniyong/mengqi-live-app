package org.mengqi.live.user.provider.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.idea.huyu.live.framework.redis.starter.key.UserProviderCacheKeyBuilder;
import org.mengqi.live.user.dto.UserDTO;
import org.mengqi.live.user.provider.dao.mapper.UserMapper;
import org.mengqi.live.user.provider.dao.po.UserPO;
import org.mengqi.live.user.provider.service.IUserService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {
    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String,UserDTO> redisTemplate;

    @Resource
    private UserProviderCacheKeyBuilder userProviderCacheKeyBuilder;

    @Resource
    private MQProducer mqProducer;

    @Override
    public UserDTO getByUserId(Long userId) {
        if(userId==null){
            return null;
        }
        String key= userProviderCacheKeyBuilder.buildUserInfoKey(userId);
        UserDTO userDTO = redisTemplate.opsForValue().get(key);
        if(userDTO!=null){
            return userDTO;
        }

        userDTO = Convert.convert(UserDTO.class, userMapper.selectById(userId));

        if(userDTO!=null){
            redisTemplate.opsForValue().set(key,userDTO,30, TimeUnit.MINUTES);
        }

        return userDTO;
    }

    @Override
    public boolean updateUserInfo(UserDTO userDTO) {
        if(userDTO ==null || userDTO.getUserId()==null){
            return false;
        }
        UserPO userPO = Convert.convert(UserPO.class, userDTO);
        userMapper.updateById(userPO);
        String key = userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId());
        redisTemplate.delete(key);

        //延时级别，1 代表延时一秒发送
        try {
            Message message = new Message();
            message.setBody(JSON.toJSONString(userDTO).getBytes());
            message.setTopic("user-update-cache");
            mqProducer.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean insert(UserDTO userDTO) {
        if(userDTO==null || userDTO.getUserId()==null){
            return false;
        }
        UserPO userPO = Convert.convert(UserPO.class, userDTO);
        userMapper.insert(userPO);
        return true;
    }

    @Override
    public Map<Long, UserDTO> batchQueryUserInfo(List<Long> userIdList) {
        if(CollectionUtil.isEmpty(userIdList)){
            return Maps.newHashMap();
        }
        userIdList=userIdList.stream().filter(id->id>10000).collect(Collectors.toList());
        if(CollectionUtil.isEmpty(userIdList)){
            return Maps.newHashMap();
        }

        //redis
        List<String> keyList=new ArrayList<>();
        userIdList.forEach(userId->{
            keyList.add(userProviderCacheKeyBuilder.buildUserInfoKey(userId));
        });

        List<UserDTO> userDTOlist = redisTemplate.opsForValue().multiGet(keyList).stream().filter(x->x!=null).collect(Collectors.toList());
        if(CollectionUtil.isNotEmpty(userDTOlist) && userDTOlist.size()==userIdList.size()){
            return userDTOlist.stream().collect(Collectors.toMap(UserDTO::getUserId, x->x));
        }
        List<Long> userIdInCacheList = userDTOlist.stream().map(UserDTO::getUserId).collect(Collectors.toList());
        List<Long> userIdNotInCacheList = userIdInCacheList.stream().filter(x -> !userIdInCacheList.contains(x)).collect(Collectors.toList());

        //多线程查询 替代union all
        Map<Long, List<Long>> userMap = userIdNotInCacheList.stream().collect(Collectors.groupingBy(userId -> userId % 100));
        List<UserDTO> userDTOList=new CopyOnWriteArrayList<>();
        userMap.values().parallelStream().forEach(queryUserIdList->{
            userDTOList.addAll(Convert.toList(UserDTO.class,userMapper.selectBatchIds(queryUserIdList))) ;
        });

        if(CollectionUtil.isNotEmpty(userDTOList)){
            Map<String,UserDTO> saveCacheKeyMap=userDTOlist.stream().collect(Collectors.toMap(userDTO -> userProviderCacheKeyBuilder.buildUserInfoKey(userDTO.getUserId()),x->x) );

            redisTemplate.opsForValue().multiSet(saveCacheKeyMap);
            redisTemplate.executePipelined(new SessionCallback<Object>(){

                @Override
                public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                    for (String redisKey : saveCacheKeyMap.keySet()) {
                        operations.expire((K) redisKey,createRandomExpireTime(),TimeUnit.SECONDS);
                    }
                    return null;
                }
            });
        }

        return userDTOList.stream().collect(Collectors.toMap(UserDTO::getUserId, x->x));
    }

    private int createRandomExpireTime(){
        int time = ThreadLocalRandom.current().nextInt();
        return time+60*30;
    }
}
