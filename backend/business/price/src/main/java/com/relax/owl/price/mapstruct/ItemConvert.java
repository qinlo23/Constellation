package com.relax.owl.price.mapstruct;


import org.mapstruct.Mapper;
// [TO_BE_DELETED] DOtoVO 注释后，以下 import 不再使用。
// import com.relax.owl.price.domain.entity.ItemDO;
// import com.relax.owl.price.domain.vo.PriceItemVO;
// import java.util.List;

/**
 * mapstruct
 *
 * @author slnt23
 * @since 2026/4/26
 */

@Mapper(componentModel = "spring")
public interface ItemConvert {
    // [TO_BE_DELETED] DOtoVO 未被调用，ItemDO 缺少 itemId/categoryName 字段（由 MyBatis XML 直接映射）。
    // List<PriceItemVO> DOtoVO(List<ItemDO> itemDOS);
}