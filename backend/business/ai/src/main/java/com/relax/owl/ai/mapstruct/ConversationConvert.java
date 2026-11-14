package com.relax.owl.ai.mapstruct;


import org.mapstruct.Mapper;
import com.relax.owl.ai.domain.entity.ConversationDO;
import com.relax.owl.ai.domain.vo.ConversationVO;

import java.util.List;

/**
 * 会话类型转化
 *
 * @author slnt23
 * @since 2026/4/18
 */

@Mapper(componentModel = "spring")
public interface ConversationConvert {

    List<ConversationVO> conversationDOListToConversationVOList(List<ConversationDO> conversationDOS);
    ConversationVO conversationDOToConversationVO(ConversationDO conversationDO);

}
