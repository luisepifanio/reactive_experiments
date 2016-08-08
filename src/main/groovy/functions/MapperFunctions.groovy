package functions

import dto.Item

/**
 * Created by lepifanio on 7/8/16.
 */
class MapperFunctions {

    public static Item mapToItemFunction(Map it) {
        Item.builder()
                .id(it.id)
                .title(it.title)
                .currencyId(it.currency_id)
                .thumbnail(it.thumbnail)
                .sellerId(it.seller_id)
                .price(it.price as BigDecimal)
                .build()
    }
}
