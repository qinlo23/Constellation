package com.relax.owl.ai.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.relax.owl.ai.domain.entity.ConversationDO;
import com.relax.owl.ai.domain.vo.ConversationVO;

import java.util.List;

/**
 * 会话管理
 *
 * @author slnt23
 * @since 2026/4/12
 */

@Mapper
public interface ConversationMapper extends BaseMapper<ConversationDO> {

    List<ConversationVO> selectByUserId(String userId);

    int updateTotalTokens(@Param("conversationId") String conversationId,
                          @Param("addTokens") Integer addTokens);
}

