package com.mycom.myapp.global.common.util;

import java.time.Duration;
import java.time.LocalDateTime;

import com.mycom.myapp.domain.reservation.dto.ReservationInsertRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReservationTimeValidator implements ConstraintValidator<ValidReservationTime, ReservationInsertRequest> {

	private final static int MAX_HOURS = 3;
	
	@Override
	public boolean isValid(ReservationInsertRequest request, ConstraintValidatorContext context) {
		LocalDateTime start = request.startTime();
		LocalDateTime end = request.endTime();
		long minutes = Duration.between(start, end).toMinutes();
		
		if (start == null || end == null) {
			return true;
		}
		
		context.disableDefaultConstraintViolation();
		
		if (start.getMinute() != 0 || start.getSecond() != 0 || end.getMinute() != 0 || end.getSecond() != 0) {
			context.buildConstraintViolationWithTemplate("예약은 정각(예: 10:00) 단위로만 가능합니다.").addConstraintViolation();
			return false;
		}
		
        if (!end.isAfter(start)) {
            context.buildConstraintViolationWithTemplate("예약 종료 시간은 시작 시간보다 나중에 가능합니다.").addConstraintViolation();
            return false;
        }
        
        if (minutes % 60 != 0) {
            context.buildConstraintViolationWithTemplate("예약 시간은 1시간 단위만 가능합니다.").addConstraintViolation();
            return false;
        }
        
        if (minutes > MAX_HOURS * 60) {
            context.buildConstraintViolationWithTemplate("예약은 최대 " + MAX_HOURS + "시간까지 가능합니다.").addConstraintViolation();
            return false;
        }
		
		return true;
	}
	
}
