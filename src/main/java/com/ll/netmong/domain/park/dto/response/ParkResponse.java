package com.ll.netmong.domain.park.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParkResponse {

    private Long id;
    private String parkNm;
    private String lnmadr;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String state;
    private String city;
    private Long likesCount;
    private Boolean isLiked;
    private Boolean petAllowed;

}
