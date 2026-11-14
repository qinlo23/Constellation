package com.relax.owl.ai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.relax.owl.ai.domain.entity.MessageDO;
import com.relax.owl.ai.domain.vo.MessageVO;

import java.util.List;

/**
 * 消息Mapper
 *
 * @author slnt23
 * @since 2026/4/12
 */

@Mapper
public interface MessageMapper extends BaseMapper<MessageDO> {

    List<MessageVO> selectByConversationId(String conversationId);

    List<MessageDO> selectRecentMessages(
            @Param("conversationId") String conversationId,
            @Param("limit") Integer limit
    );
}

