package dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Item {
    String id;
    String title;
    BigDecimal price;
    String currencyId;
    String thumbnail;
    Long sellerId;

    public ItemBuilder asBuilder(){
        return Item.builder()
                .id(id)
                .title(title)
                .price(price)
                .currencyId(currencyId)
                .thumbnail(thumbnail)
                .sellerId(sellerId);

    }
}
