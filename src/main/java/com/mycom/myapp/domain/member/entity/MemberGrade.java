package com.mycom.myapp.domain.member.entity;

import lombok.Getter;

@Getter
public enum MemberGrade {

    BRONZE(0, 0),
    SILVER(5, 5),
    GOLD(10, 10),
    VIP(20, 15);

    private final int minReservationCount;
    private final int discountPercent;

    MemberGrade(int minReservationCount, int discountPercent) {
        this.minReservationCount = minReservationCount;
        this.discountPercent = discountPercent;
    }

    public int applyDiscount(int price) {
        return price - (price * discountPercent / 100);
    }

    public static MemberGrade of(long confirmedReservationCount) {
        MemberGrade result = BRONZE;
        for (MemberGrade grade : values()) {
            if (confirmedReservationCount >= grade.minReservationCount) {
                result = grade;
            }
        }
        return result;
    }
}
