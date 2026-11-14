package com.relax.owl.price.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.relax.owl.price.bloom.service.BloomFilterService;
import com.relax.owl.price.domain.entity.ItemDO;
import com.relax.owl.price.service.ItemService;

import java.util.List;

import static com.relax.owl.price.constant.CacheConstant.BLOOM_ITEM_PREFIX;

/**
 * price 模块布隆过滤器初始化
 * 启动时将所有 itemId 注册进去
 *
 * @author slnt23
 * @since 2026/5/30
 */

@Component
@RequiredArgsConstructor
public class PriceBloomInitializer {

    private final BloomFilterService bloomFilter;
    private final ItemService itemService;

    @PostConstruct
    public void init() {
        List<ItemDO> items = itemService.list();
        for (ItemDO item : items) {
            bloomFilter.add(BLOOM_ITEM_PREFIX + item.getId());
        }
    }
}
