package dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    String id;
    String nickname;
    String siteStatus;
    Long points;

}
